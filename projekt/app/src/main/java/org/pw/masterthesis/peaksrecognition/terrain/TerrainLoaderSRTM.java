package org.pw.masterthesis.peaksrecognition.terrain;

import android.content.Context;
import android.content.res.AssetManager;

import org.pw.masterthesis.peaksrecognition.Config;
import org.pw.masterthesis.peaksrecognition.managers.CoordsManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

public class TerrainLoaderSRTM implements TerrainLoader {
    private final Context context;
    private final Config config;

    public TerrainLoaderSRTM(Context context, Config config) {
        this.context = context;
        this.config = config;
    }

    public LoadedTerrain load() {
        Vector<int[]> coords = prepareCoords(config.initObserverLocation[0], config.initObserverLocation[1], config.maxDistance);
        int[][] coordsRange = getCoordsRange(coords);
        double[][] worldGridSize = calcWorldSize(coordsRange);
        double[] gridSize = worldGridSize[1];
        String[][] filesNamesGrid = prepareFilesNamesGrid(coords, (int) config.initObserverLocation[0], (int) config.initObserverLocation[1]);

        short[][] heightMap = loadHgtGrid(filesNamesGrid, config.initHgtSize, config.simplifyFactor);
        int[] loadedHgtSize = new int[]{heightMap.length, heightMap[0].length};
        double[] scale = calcScale(loadedHgtSize, worldGridSize[0]);

        return new LoadedTerrain(heightMap, coordsRange, gridSize, loadedHgtSize, scale);
    }

    private short[][] loadHgtFile(String path, int initHgtSize, int simplifyFactor, int initSimplifiedHgtSize) {
        byte[] data;
        try {
            data = loadHgtByteData(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return convertHgtByteToArray(data, initHgtSize, simplifyFactor, initSimplifiedHgtSize);
    }

    private short[][] loadHgtGrid(String[][] filesNamesGrid, int initHgtSize, int simplifyFactor) {
        int startRow = -1;
        int startCol = -1;
        int endRow = -1;
        int endCol = -1;

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                if (filesNamesGrid[i][j] != null) {
                    if (startRow == -1) {
                        startRow = i;
                    }
                    if (startCol == -1) {
                        startCol = j;
                    }
                    if (endRow < i) {
                        endRow = i;
                    }
                    if (endCol < j) {
                        endCol = j;
                    }
                }
            }
        }

        int rows = endRow - startRow + 1;
        int cols = endCol - startCol + 1;

        int initSimplifiedHgtSize = calcNewHgtSize(initHgtSize, simplifyFactor);
        short[][] elevation = new short[rows * initSimplifiedHgtSize][cols * initSimplifiedHgtSize];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                short[][] fileData = loadHgtFile("srtm_data/" + filesNamesGrid[i + startRow][j + startCol],
                        initHgtSize, simplifyFactor, initSimplifiedHgtSize);

                int xOffset = i * fileData.length;
                int zOffset = j * fileData[0].length;

                for (int x = 0; x < fileData.length; ++x) {
                    int rowIndex = x + xOffset;
                    for (int z = 0; z < fileData[0].length; ++z) {
                        int colIndex = z + zOffset;
                        short val = fileData[x][z];
                        elevation[rowIndex][colIndex] = val;
                    }
                }
                fileData = null;
            }
        }

        return elevation;
    }

    private byte[] loadHgtByteData(String path) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(path);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] data = new byte[bufferedInputStream.available()];
        //noinspection ResultOfMethodCallIgnored
        bufferedInputStream.read(data);
        bufferedInputStream.close();

        return data;
    }

    private short[][] convertHgtByteToArray(byte[] data, int initHgtSize, int simplifyFactor, int initSimplifiedHgtSize) {
        short[][] heightMap = new short[initSimplifiedHgtSize][initSimplifiedHgtSize];
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        int arrayRow;
        int arrayCol;

        for (int i = 0; i < initHgtSize; ++i) {
            arrayCol = 0;
            if (i % simplifyFactor == 0) {
                arrayRow = i / simplifyFactor;
            } else {
                arrayRow = -1;
            }
            for (int j = 0; j < initHgtSize; ++j) {
                short value = byteBuffer.getShort();

                if (arrayRow != -1 && j % simplifyFactor == 0) {
                    heightMap[arrayRow][arrayCol] = value;
                    ++arrayCol;
                }
            }
        }

        return heightMap;
    }

    private double[] calcScale(int[] loadedHgtSize, double[] worldSize) {
        double xScale = worldSize[0] / (loadedHgtSize[0] - 1);
        double yScale = (1.0 / 1000.0);
        double zScale = worldSize[1] / (loadedHgtSize[1] - 1);

        return new double[]{xScale, yScale, zScale};
    }

    private String convertCoordsToFileName(int latitude, int longitude) {
        String latitudeStr = String.valueOf(Math.abs(latitude));
        String longitudeStr = String.valueOf(Math.abs(longitude));

        if (latitude > -10 && latitude < 10) {
            latitudeStr = "0" + latitudeStr;
        }

        if (longitude > -100 && longitude < 100) {
            if (longitude > -10 && longitude < 10) {
                longitudeStr = "00" + longitudeStr;
            } else {
                longitudeStr = "0" + longitudeStr;
            }
        }
        String fileName;
        if (latitude >= 0) {
            fileName = "N" + latitudeStr;
        } else {
            fileName = "S" + latitudeStr;
        }

        if (longitude >= 0) {
            fileName += "E" + longitudeStr;
        } else {
            fileName += "W" + longitudeStr;
        }

        return fileName;
    }

    private Vector<int[]> prepareCoords(double observerLatitude, double observerLongitude, double maxDistance) {
        int latitudeInt = (int) observerLatitude;
        int longitudeInt = (int) observerLongitude;
        Vector<double[]> coordsToCheck = new Vector<>();
        Vector<int[]> coordsForFilesNames = new Vector<>();
        double[] y;
        double[] x;

        if (observerLatitude > 0.0) {
            y = new double[]{latitudeInt, observerLatitude, latitudeInt + 1.0};
        } else {
            y = new double[]{latitudeInt - 1, observerLatitude, latitudeInt};
        }

        if (observerLongitude > 0.0) {
            x = new double[]{longitudeInt, observerLongitude, longitudeInt + 1.0};
        } else {
            x = new double[]{longitudeInt - 1, observerLongitude, longitudeInt};
        }

        if (observerLatitude == latitudeInt && observerLongitude == longitudeInt) {
            for (int i = 0; i < 2; ++i) {
                for (int j = 0; j < 2; ++j) {
                    coordsForFilesNames.add(new int[]{latitudeInt - i, longitudeInt - j});
                }
            }
        } else if (observerLatitude == latitudeInt) {
            coordsForFilesNames.add(new int[]{latitudeInt - 1, (int) x[0]});
            coordsForFilesNames.add(new int[]{latitudeInt, (int) x[0]});

            for (int i = 0; i < 2; ++i) {
                coordsToCheck.add(new double[]{latitudeInt, x[0], latitudeInt - i, x[0] - 1});
                coordsToCheck.add(new double[]{latitudeInt, x[2], latitudeInt - i, x[0] + 1});
            }
        } else if (observerLongitude == longitudeInt) {
            coordsForFilesNames.add(new int[]{(int) y[0], longitudeInt - 1});
            coordsForFilesNames.add(new int[]{(int) y[0], longitudeInt});

            for (int i = 0; i < 2; ++i) {
                coordsToCheck.add(new double[]{y[0], longitudeInt, y[0] - 1, longitudeInt - i});
                coordsToCheck.add(new double[]{y[1], longitudeInt, y[0] + 1, longitudeInt - i});
            }
        } else {
            coordsForFilesNames.add(new int[]{latitudeInt, longitudeInt});

            for (int i = 0; i < 3; ++i) { // latitude

                double yCoordToCheck = y[i];
                int yCoordFile = (int) y[0] + (i - 1);

                for (int j = 0; j < 3; ++j) { // longitude
                    if (!(i == 1 && j == 1)) {
                        double xCoordToCheck = x[j];
                        int xCoordFile = (int) x[0] + (j - 1);
                        coordsToCheck.add(new double[]{yCoordToCheck, xCoordToCheck, yCoordFile, xCoordFile});
                    }
                }
            }
        }

        for (double[] coordsData : coordsToCheck) {
            double distance = CoordsManager.equirectangularApproximation(coordsData[0], coordsData[1],
                    observerLatitude, observerLongitude);
            if (distance <= maxDistance) {
                coordsForFilesNames.add(new int[]{(int) coordsData[2], (int) coordsData[3]});
            }
        }

        return coordsForFilesNames;
    }

    private int[][] getCoordsRange(Vector<int[]> coords) {
        int minLatitude = Integer.MAX_VALUE;
        int maxLatitude = Integer.MIN_VALUE;
        int minLongitude = Integer.MAX_VALUE;
        int maxLongitude = Integer.MIN_VALUE;

        for (int[] coord : coords) {
            minLatitude = Math.min(minLatitude, coord[0]);
            maxLatitude = Math.max(maxLatitude, coord[0]);
            minLongitude = Math.min(minLongitude, coord[1]);
            maxLongitude = Math.max(maxLongitude, coord[1]);
        }
        ++maxLatitude;
        ++maxLongitude;

        return new int[][]{{minLatitude, maxLatitude}, {minLongitude, maxLongitude}};
    }

    private double[][] calcWorldSize(int[][] coordsRange) {
        int[] latitudeRange = coordsRange[0];
        int[] longitudeRange = coordsRange[1];
        double totalLatitudeDistance = 0.0;
        double totalLongitudeDistance = 0.0;

        for (int i = 0; i < 2; ++i) {
            totalLatitudeDistance += CoordsManager.equirectangularApproximation(latitudeRange[0], longitudeRange[i],
                    latitudeRange[1], longitudeRange[i]);
            totalLongitudeDistance += CoordsManager.equirectangularApproximation(latitudeRange[i], longitudeRange[0],
                    latitudeRange[i], longitudeRange[1]);
        }

        double meanLatitudeDistance = totalLatitudeDistance / 2.0;
        double meanLongitudeDistance = totalLongitudeDistance / 2.0;
        double latitudeGridSize = meanLatitudeDistance / (double) Math.abs(latitudeRange[1] - latitudeRange[0]);
        double longitudeGridSize = meanLongitudeDistance / (double) Math.abs(longitudeRange[1] - longitudeRange[0]);

        return new double[][]{{meanLatitudeDistance, meanLongitudeDistance}, {latitudeGridSize, longitudeGridSize}};
    }

    private String[][] prepareFilesNamesGrid(Vector<int[]> coords, int centralLatitude, int centralLongitude) {
        String[][] filesNamesGrid = new String[][]{
                {null, null, null},
                {null, null, null},
                {null, null, null},
        };
        for (int[] coord : coords) {
            int latitudeDiff = coord[0] - centralLatitude;
            int longitudeDiff = coord[1] - centralLongitude;
            filesNamesGrid[1 - latitudeDiff][1 + longitudeDiff] = convertCoordsToFileName(coord[0], coord[1]) + ".hgt";
        }
        return filesNamesGrid;
    }

    private int calcNewHgtSize(int initHgtSize, int simplifyFactor) {
        int newHgtSize = initHgtSize / simplifyFactor;
        if (initHgtSize % simplifyFactor != 0) {
            ++newHgtSize;
        }

        return newHgtSize;
    }
}