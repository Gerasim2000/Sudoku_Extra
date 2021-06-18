@if not DEFINED IS_MINIMIZED set IS_MINIMIZED=1 && start "" /min "%~dpnx0" %* && exit
@echo MATHDOKU IS CURRENTLY RUNNING
@echo DO NOT CLOSE THIS WINDOW!
@echo THIS WINDOW WILL AUTOMATICALLY CLOSE WHEN YOU EXIT MATHDOKU
@SET PATH=%JDK_HOME%\bin;%PATH%
@set PATH_TO_FX="C:\Program Files\Java\javafx-sdk-11.0.2\lib"
@javac -encoding UTF-8 --module-path %PATH_TO_FX% --add-modules javafx.controls Main.java
@java --module-path %PATH_TO_FX% --add-modules javafx.controls Main
exit