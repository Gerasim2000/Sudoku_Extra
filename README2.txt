Follow the instructions below!

1. In the command prompt, go to the file location of MathDoku

2. Now type: set PATH_TO_FX="path\to\javafx-sdk-11.0.2\lib"

* Use your own path PATH_TO_FX="<path>" to the location of javafx-sdk-11.0.2\lib
* You must have JavaFX 11 and above to be able to run the MathDoku
* Example:set PATH_TO_FX="C:\Program Files\Java\javafx-sdk-11.0.2\lib"

3. Now use the following command: javac -encoding UTF-8 --module-path %PATH_TO_FX% --add-modules javafx.controls Main.java

4. After that enter: java --module-path %PATH_TO_FX% --add-modules javafx.controls Main

I have created a .bat file to run these commands!
It is called RUN.bat
If you would like to use it you have to set the path to your JavaFX 11 on line 6