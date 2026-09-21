' ====================================================================
' FinCore Silent Windows GUI Launcher
' Launches the Java Swing Finance Dashboard without opening a CMD console
' ====================================================================
Set objShell = CreateObject("WScript.Shell")
Set objFSO = CreateObject("Scripting.FileSystemObject")

strScriptDir = objFSO.GetParentFolderName(WScript.ScriptFullName)
strBatPath = strScriptDir & "\run.bat"

' 0 = Hide window, False = Do not wait for completion
objShell.Run Chr(34) & strBatPath & Chr(34) & " --gui", 0, False
