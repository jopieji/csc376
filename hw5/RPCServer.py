from xmlrpc.server import SimpleXMLRPCServer
import os

def sort_list(nums):
    sorted_nums = sorted(nums)
    return sorted_nums

def print_message(message):
    print(message)
    messages_list.append(message)
    return messages_list

def get_file(filename):
    if not os.path.exists(filename):
      return -1 
    with open(filename, 'r') as file:
       content = file.read()
       return content

server = SimpleXMLRPCServer(("localhost", 6789))
server.register_function(sort_list, "sort_list")
server.register_function(print_message, "print_message")
server.register_function(get_file, "get_file")
messages_list = []
server.serve_forever()

