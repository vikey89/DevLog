# VHS demo tape — https://github.com/charmbracelet/vhs
#
# Prerequisites:
#   ./scripts/setup-demo.sh
#   ./gradlew installDist
#
# Run: vhs demo.tape
# Cleanup: ./scripts/setup-demo.sh --cleanup

Output demo.gif

Set FontSize 14
Set Width 1200
Set Height 900
Set Theme "Catppuccin Mocha"
Set TypingSpeed 50ms
Set Shell "zsh"

Hide
Type "export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
Enter
Type "export PATH=./build/install/devlog/bin:$PATH"
Enter
Type "clear"
Enter
Sleep 500ms
Show

Sleep 500ms
Type "devlog week"
Enter
Sleep 20s
