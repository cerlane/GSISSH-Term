#!/bin/sh
#
# Sshtools - Java SSH2 API
#
# Copyright (C) 2002 Lee David Painter.
#
# Written by: 2002 Lee David Painter <lee@sshtools.com>
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU Library General Public License
# as published by the Free Software Foundation; either version 2 of
# the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Library General Public License for more details.
#
# You should have received a copy of the GNU Library General Public
# License along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

# OS specific support.  $var _must_ be set to either true or false.
CYGWIN=false;
DARWIN=false;
case "`uname`" in
  CYGWIN*) CYGWIN=true ;;
  Darwin*) DARWIN=true
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
           fi
           ;;
esac

if [ -z "$SSHTOOLS_HOME" ] 
then SCRIPT="$0"
     BASENAME=`basename $0`
     CWD=`pwd`
     cd `dirname "$SCRIPT"`
     while [ -h "$PRG" ] 
     do
         LS=`ls -ld "$SCRIPT"`
         LINK=`expr "$LS" : '.*-> \(.*\)$'`
         if expr "$LINK" : '.*/.*' > /dev/null
         then SCRIPT="$LINK"
         else SCRIPT=`dirname "$SCRIPT"`"/$LINK"
         fi
     done
     SSHTOOLS_HOME=`dirname "$SCRIPT"`/..
     cd "$CWD"
     SSHTOOLS_HOME=`cd "$SSHTOOLS_HOME" && pwd`
fi

if [ $CYGWIN = true ]
then [ -n "$ANT_HOME" ] && SSHTOOLS_HOME=`cygpath --unix "$SSHTOOLS_HOME"`
     [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
     [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

if [ -n "$JAVA_HOME"  ] 
then if [ -x "$JAVA_HOME/jre/sh/java" ] 
     then JAVACMD="$JAVA_HOME/jre/sh/java"
     else JAVACMD="$JAVA_HOME/bin/java"
     fi
else JAVACMD=java
fi
 
SSHTOOLS_CLASSPATH=
for i in ${SSHTOOLS_HOME}/classes ${SSHTOOLS_HOME}/dist/*.jar "${SSHTOOLS_HOME}"/lib/*.jar "${SSHTOOLS_HOME}"/lib/jglobus2/*.jar
do
    if [ -z "$SSHTOOLS_CLASSPATH" ] 
    then SSHTOOLS_CLASSPATH="$i"
    else SSHTOOLS_CLASSPATH="$i":"$SSHTOOLS_CLASSPATH"
    fi
done

if [ "$CWGWIN" = "true" ]
then SSHTOOLS_HOME=`cygpath --path --windows "$ANT_HOME"`
     JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
     SSHTOOLS_CLASSPATH=`cygpath --path --windows "$SSHTOOLS_CLASSPATH"`
     CYGHOME=`cygpath --path --windows "$HOME"`
fi

"$JAVACMD" -classpath "$SSHTOOLS_CLASSPATH" -Dsshterm.xForwarding.localDisplay=${DISPLAY} -Dsshtools.home="${SSHTOOLS_HOME}" -Dlog4j.properties=sshterm.properties com.sshtools.sshterm.SshTerm $*

SSHTOOLS_HOME=""
SSHTOOLS_CLASSPATH=""
