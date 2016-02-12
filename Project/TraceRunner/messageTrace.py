import os
import sys
import subprocess

port = sys.argv[1]

#get pid
print "***Get PID***"
res = subprocess.Popen(['adb', 'jdwp'],stdout=subprocess.PIPE)

pid = res.stdout.readline()
res.terminate() #TODO: this will probably fail if two debuggable processes are running
pid = int(pid)

print pid

#open bridge
print "***Open Bridge***"
res = subprocess.call(['adb', 'forward', 'tcp:'+port, 'jdwp:' + str(pid)])
if 0 != res:
    raise Exception("bridge failed")