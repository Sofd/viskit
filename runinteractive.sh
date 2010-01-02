#!/bin/sh

. "`dirname $0`/init_runinteractive.sh"

java -cp "$BSH_CP" bsh.Interpreter "$@"

# TODO: it would be preferable to define an Ant task for this in
# build.xml (thereby inheriting NB's project classpath settings), but
# Ant can't run Beanshell properly in interactive mode (all output of
# Beanshell, including the Beanshell prompt, is captured by Ant,
# passed through only line-wise, and with Ant's output logging blurb
# prepended)
