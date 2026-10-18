#!/bin/bash

if [ "$1" = "debug" ]; then
    args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
fi

java $args -cp /home/rostom/src/crafting-interpreters/jlox/build/classes/java/main/ com.jlox.Jlox  $2 2> >(grep -v '^Picked up _JAVA_OPTIONS:')