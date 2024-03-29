#! /usr/bin/env python
# -*- coding: utf-8 -*-
# vi:ts=4:et

import urllib
import pycurl
import sys

SECRET = "0EMK7Rwz"
#SECRET = "111"

def create_users(minnusers, maxnusers, name, secret):
  """
  http://localhost:9090/plugins/userService/userservice?type=add&secret=bigsecret&username=kafka&password=drowssap&name=franz&email=franz@kafka.com
    
  """
  for user in range(minnusers, maxnusers):
    print "user %s%s" % (name, user)
    args = {'type':'add', 'secret': secret, 'username': '%s%s' % (name, user), 
            'password':'%s%spass' % (name, user)}
    c = pycurl.Curl()
    c.setopt(c.URL, 'http://localhost:9090/plugins/userService/userservice')
#    c.setopt(pycurl.USERPWD, "%s:%s" % (USER,PASS))
    c.setopt(c.POSTFIELDS, urllib.urlencode(args))
    c.setopt(c.VERBOSE, 1)
    c.perform()
    c.close()

def delete_users(nusers, secret):
  """
  """
  for user in range(100,nusers):
    args = {'type':'delete', 'secret': secret, 'username':'tsung%s' % user}
    c = pycurl.Curl()
    c.setopt(c.URL, 'http://localhost:9090/plugins/userService/userservice')
#    c.setopt(pycurl.USERPWD, "%s:%s" % (USER,PASS))
    c.setopt(c.POSTFIELDS, urllib.urlencode(args))
    print urllib.urlencode(args)
    c.setopt(c.VERBOSE, 1)
    c.perform()
    c.close()

def main(argv):
    create_users(1, 1001, "sub", SECRET)

if __name__ == "__main__":
    main(sys.argv[1:])
