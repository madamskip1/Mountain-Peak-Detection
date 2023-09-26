

# Praca dyplomowa magisterska "*Zastosowanie algorytmów cyfrowego przetwarzania obrazów do identyfikacji szczytów górskich w czasie rzeczywistym*"

**Autor:** Adamski Maciej<br/>
**Promotor:** prof. dr hab. inż. Przemysław Rokita<br/>
**Uczelnia/wydział/instytut**: Instytut Informatyki na Wydziale Elektroniki i Nauk Informacyjnych, Politechniki Warszawskiej<br />
**Tytuł**: Zastosowanie algorytmów cyfrowego przetwarzania obrazów do identyfikacji szczytów górskich w czasie rzeczywistym.<br />
**Tytuł angielski**: Applying digital image processing algorithms in real time mountain peaks labelling.

 
 ## Streszczenie

<p align="justify"> Celem niniejszej pracy dyplomowej magisterskiej było zbadanie oraz przetestowanie algorytmów cyfrowego przetwarzania obrazów do identyfikacji szczytów górskich na obrazie w czasie rzeczywistym. Prace badawcze były realizowane na stworzonej na potrzeby projektu platformie testowej na urządzeniach mobilnych z systemem Android. Opracowany proces rozpoznawania gór może  zostać wykorzystany również w innych konfiguracjach sprzętowych.

Opierając się na analizie literatury oraz podobnych, istniejących już rozwiązaniach przygotowany został wstępny proces rozpoznawania szczytów, który w dalszych iteracjach prac badawczych był modyfikowany dzięki poszerzanej wiedzy, a także na podstawie wyników testów oraz porównań algorytmów. Została przygotowana na potrzeby pracy dyplomowej aplikacja, która w momencie wykonywania zdjęcia zapisuje dodatkowe dane geolokalizacyjne na jego temat. Dzięki temu stworzono zbiór statycznych zdjęć gór z odpowiednimi danymi. Zasilana nimi platforma testowa pozwalała sprawdzić działanie przygotowanych metod, a także wybrać optymalne w kontekście procesu rozpoznawania szczytów górskich. Testy były zorientowane na czas wykonania algorytmów związany z działaniem projektu w czasie rzeczywistym, ale również na ich jakość i skuteczność.

Proces rozpoznawania szczytów górskich zaproponowany w ramach pracy dyplomowej składa się z kilku elementów. Na początku zbierane są dane z sensorów urządzenia. Na ich podstawie generowany jest trójwymiarowy model płaszczyzny Ziemi z perspektywy obserwatora. Wykorzystywane do tego są *numeryczne modele terenu* oraz interfejs *OpenGL*. Na tak wygenerowanej panoramie określane są widoczne góry. W tym celu przeprowadzane jest filtrowanie danych dotyczących szczytów górskich przy pomocy metod usuwania powierzchni niewidocznych - *frustum culling* oraz *occlusion culling*. Wykorzystując algorytm detekcji krawędzi *Canny* tworzone są na podstawie panoramy oraz zdjęcia wejściowego obrazy binarne zawierające kontury gór. Przy użyciu metody *dopasowania obrazów na podstawie szablonu* porównywane są odpowiednie fragmenty zdjęć potencjalnie zawierające dane szczyty. Na podstawie uzyskanych wyników stwierdzana jest widoczność poszczególnych szczytów górskich na kolejnych klatkach nagrania. 

Opracowany proces został poddany weryfikacji końcowej w warunkach rzeczywistych. Potwierdziła ona zdolność takiego cyklu do prawidłowej identyfikacji szczytów górskich oraz wykazała możliwość jej działania w czasie rzeczywistym. </p>

**Słowa kluczowe**: rozpoznawanie szczytów górskich, rejestracja obrazu, wyrównanie zdjęć, dopasowanie obrazu na podstawie szablonu, numeryczne modele terenu, grafika trójwymiarowa, przetwarzanie cyfrowe obrazu, czas rzeczywisty

## Streszczenie angielskie

<p align="justify"> The main aim of the Master's Degree Thesis was to research and test digital image processing algorithms which allow to recognize mountain peaks in the video in real-time. The experiments were conducted on the custom made platform that was made for mobile devices with the Android operating system. The developed mountain peaks recognition process can also be transferred to different platforms.

Based on the analysis of the current state of the art and other existing approaches, an early process of mountain peak recognition was prepared. In the next stages of the conducted research the process has been updated due to growing knowledge. Moreover, it was also improved with numerous tests and comparisons of different algorithms. Furthermore, for the sake of the thesis there was also created an simple application, which saves the additional geolocation data at the moment a picture is taken. Hence, a set of static photos of mountain peaks with appropriate data was created. The application was loaded with this set. It allowed to evaluate the quality of the different methods. What is more, it helps to choose optimal methods in terms of the mountain peak recognition process. Tests were mainly focused on algorithms time complexity in real-time. However, the quality and accuracy of the solutions were also taken into account.

The process of the mountain peaks recognition that was proposed in this thesis is made of several elements. At the very beginning, the data is gathered from the device's sensors. It is used to generate a 3D model of the Earth's surface from the observer's point of view. The model is created with the *digital elevation model* and *OpenGL* interface. With the generated panorama view, the visibility of mountain peaks are assessed. So as to do that, there is a need to filter the data concerning mountain peaks. The hidden-surface determination methods are used - *frustum culling* and *occlusion culling*. With the *Canny* edge detector algorithm, on the basis of the panorama view and the input photo, the binary images containing the mountain's contour are created. *Template matching* is used to compare specific parts of the images that might include possible peaks. Obtained results are used to conclude the visible mountain peaks in the consecutive frames of the video.

Developed process was verified in the real-life conditions. The process's ability to properly recognize mountain peaks was confirmed. Furthermore, it was shown to work in the real-time. </p>

**Keywords**: mountain peaks recognition, image registration, image alignment, template matching, digital elevation model, three-dimensional graphics, digital image processing, real-time

 ## Katalogi

- */projekt/*  - kod aplikacji  
- */praca_dyplomowa/*  - praca dyplomowa w formacie LaTeX oraz skompilowana do postaci dokumentu pdf W szczególności:
  - Wygenerowana praca dyplomowa w formacie PDF
  - Kod źródłowy pracy dyplomowej w formacie LaTeX (TeX)
- */materialy_prezentacyjne/*  - materiał prezentacyjne, w szczególności nagranie pokazujące działanie projektu w czasie rzeczywistym
- */testy_wyniki/* - wyniki poszczególnych testów w formacie csv


## Instrukcja kompilacji

 1. W programie *Android Studio* utworzyć nowy projekt wykorzystując opcję *Get from VCS*.
 2. Wybrać następujące opcje
 -- Version control: `Git`
 -- URL:  `https://github.com/madamskip1/Mountain-Peak-Detection.git`.
 3. W oknie *Build Variants* wybrać tryb budowania `release` dla modułów: *:app* oraz *:opencv*.
 4. W konfiguracji *Run/Debug Configurations* w zakładce *General/Installation Options* ustawić flagę instalacji (*Install Flags*) na `-g`.
 5. Projekt jest gotowy do utworzenia, a następnie instalacji na urządzeniu mobilnym.
