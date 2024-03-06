import xmlrpc.client

proxy = xmlrpc.client.ServerProxy("http://localhost:6789/")

def prompt_user():
    print("Enter an option ('s', 'm', 'f', 'x'):\n    (S)ort numbers\n    (M)essage (send)\n    (F)ile (request)\n   e(X)it")
    opt = input()
    if opt in ["s", "m", "f", "x"]: return opt
    else: return -1

while True:
    inp = prompt_user()
    if inp == -1:
       continue
    match inp:
       case "s":
          print("Enter your numbers:")
          inp = input().split()
          nums_to_sort = [int(n) for n in inp]
          sorted_nums = proxy.sort_list(nums_to_sort)
          string_to_print = ""
          for num in sorted_nums:
             string_to_print = string_to_print + str(num) + " "
          print(string_to_print)
       case "m":
          print("Enter your message:")
          inp = input()
          l = proxy.print_message(inp)
          for item in l:
             print(item)
       case "f":
         print("Which file do you want?")
         file_name = input()
         file_data = proxy.get_file(file_name)
         if file_data == -1:
            print("File does not exist")
         else:
             with open(file_name, 'w') as file:
                 file.write(file_data)
       case "x":
         break
