#!/bin/bash


java -cp /home/rostom/src/crafting-interpreters/jlox/build/classes/java/main/ com.jlox.Jlox  $1 2> >(grep -v '^Picked up _JAVA_OPTIONS:')