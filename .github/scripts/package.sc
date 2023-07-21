//> using scala 3
//> using dep com.lihaoyi::os-lib:0.9.1

import scala.util.Properties

val platformSuffix: String = {
  val os =
    if Properties.isWin then "pc-win32"
    else if Properties.isLinux then "pc-linux"
    else if Properties.isMac then "apple-darwin"
    else sys.error(s"Unrecognized OS: ${sys.props("os.name")}")
  os
}
val appName = "tgtg"
val artifactsPath = os.Path("artifacts", os.pwd)
val destPath =
  if Properties.isWin then artifactsPath / s"$appName-$platformSuffix.exe"
  else artifactsPath / s"$appName-$platformSuffix"
val scalaCLILauncher =
  if Properties.isWin then "scala-cli.bat" else "scala-cli"

os.makeDir(artifactsPath)
os.proc(scalaCLILauncher, "--power", "package", ".", "-o", destPath, "--native-image")
  .call(cwd = os.pwd)
  .out
  .text()
  .trim
