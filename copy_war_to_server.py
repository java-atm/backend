#!/usr/bin/env python3

import os

# Authorisation properties
PATH_TO_PEM_FILE = "atm.pem"

# Destination properties
USERNAME_OF_THE_SERVER = "ubuntu"
IP_OF_THE_SERVER = "ec2-3-129-17-241.us-east-2.compute.amazonaws.com"
PATH_OF_THE_DESTINATION = "/usr/share/apache-tomcat-9.0.41/webapps"

HOST = USERNAME_OF_THE_SERVER + "@" + IP_OF_THE_SERVER + ":"
FULL_DESTINATION_PATH = HOST + PATH_OF_THE_DESTINATION

PATH_OF_THE_WAR_FILE = "target/webapps/backend.war"

if not os.path.isfile(PATH_TO_PEM_FILE):
    raise RuntimeError("PEM file is not here, fix it you idiot, copy it to the current directory")

if not os.path.isfile(PATH_OF_THE_WAR_FILE):
    raise RuntimeError("WAR FILE DOES NOT EXIST %s" % PATH_OF_THE_WAR_FILE)

cmd = "scp -i %s %s %s" % (PATH_TO_PEM_FILE, PATH_OF_THE_WAR_FILE, FULL_DESTINATION_PATH)
result = os.system(cmd)
if result == 0:
    print("Successfully copied %s" % PATH_OF_THE_WAR_FILE)
    exit(0)
else:
    print("FAILED: Copy command returned %s exit status" % result)
    exit(1)
