#!/usr/bin/python
# -*- coding: iso-8859-1 -*-

from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import urlparse
import re, os, cgi
import serial
import RPi.GPIO as GPIO
import time
import struct

# Define server address and port, use localhost if you are running this on your Mattermost server.
HOSTNAME = '192.168.0.44'
PORT = 80

ser=serial.Serial("/dev/ttyACM0",9600)
ser.baudrate=9600

# guarantee unicode string
_u = lambda t: t.decode('UTF-8', 'replace') if isinstance(t, str) else t


class PostHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        args = {}
        idx = self.path.find('?')
        if idx >= 0:
            rpath = self.path[:idx]
            args = cgi.parse_qs(self.path[idx+1:])
        else:
            rpath = self.path

        # Get the file path.
        basepath = "/var/www/html/"
        path = basepath + rpath
        dirpath = None
        # If it is a directory look for index.html
        # or process it directly if there are 3
        # trailing slashed.
        if os.path.exists(path) and os.path.isdir(path):
            dirpath = path  # the directory portion
            index_files = ['index.html']
            for index_file in index_files:
                tmppath = basepath + index_file
                if os.path.exists(tmppath):
                    path = tmppath
                    break


        if os.path.exists(path) and os.path.isfile(path):
            # This is valid file, send it as the response
            # after determining whether it is a type that
            # the server recognizes.
            _, ext = os.path.splitext(path)
            ext = ext.lower()
            content_type = {
                '.css': 'text/css',
                '.gif': 'image/gif',
                '.htm': 'text/html',
                '.html': 'text/html',
                '.jpeg': 'image/jpeg',
                '.jpg': 'image/jpg',
                '.js': 'text/javascript',
                '.png': 'image/png',
                '.text': 'text/plain',
                '.txt': 'text/plain',
                '.csv': 'text/csv',
                '.woff2': '',
                '.ico': 'image/x-icon',
            }
            # If it is a known extension, set the correct
            # content type in the response.
            if ext in content_type:
                self.send_response(200)  # OK
                if ext in ['.css','.js','.woff2','.ico']:
                    self.send_header('Cache-Control', 'max-age=3110400')
                    self.send_header('Expires', 'Mon, 13 Aug 2199 15:10:03 GMT')
                self.send_header('Content-type', content_type[ext])
                self.end_headers()
                with open(path) as ifp:
                    self.wfile.write(ifp.read())



    def do_POST(self):
        """Respond to a POST request."""
        # Extract the contents of the POST
        length = int(self.headers['Content-Length'])
        post_data = urlparse.parse_qs(self.rfile.read(length))

        for key, value in post_data.iteritems():
            value = re.sub('[\[\]\']', '', str(value))
            if key == 'method':
		if value == "toggle_lights":
                    ser.write(struct.pack('>B',255))
                elif value == "open_curtain":
                    ser.write(struct.pack('>B',254))
                elif value == "close_curtain":
                    ser.write(struct.pack('>B',253))
                elif value == "move_curtain":
                    ser.write(struct.pack('>B',int(post_data['slider'][0])))

        self.do_GET()

if __name__ == '__main__':
    server = HTTPServer((HOSTNAME, PORT), PostHandler)
    print('Starting bedroom server, use <Ctrl-C> to stop')
    server.serve_forever()


