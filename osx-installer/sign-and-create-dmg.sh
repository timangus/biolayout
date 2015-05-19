#! /bin/sh

# This takes a Miru.app in the cwd, signs it and puts it in a dmg
#
# Prerequisites:
#
#	"Kajeka Limited" Apple Developer ID cert on the keychain
#	appdmg (brew install node && npm install -g appdmg)

codesign -f -v -s "Kajeka Limited" Miru.app
rm Miru.dmg && appdmg Miru.dmg.spec.json Miru.dmg
