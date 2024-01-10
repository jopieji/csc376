import sys

def check_args_and_parse(args_arr):
    if "-o" in args_arr:
        index = args_arr.index("-o")
        print("-o: " + args_arr[index+1])
    if "-t" in args_arr:
        index = args_arr.index("-t")
        print("-t: " + args_arr[index+1])
    if "-h" in args_arr:
        print("-h: ")

# eat first arg: name of program
name = sys.argv[0]

# check for hyphens for flags
# -h has no input after
# for -o and -t, I need to grab the arguments

# echo anything inputted to standard input

# exit on Ctrl-d (this is default behavior for python on mac)

args = []
for arg in sys.argv:
    args.append(arg)

print("Standard Input:")
# read from standard input
text= sys.stdin.readline() # leaves the newline char at the end
sys.stdout.write(text)

while text:
    text= sys.stdin.readline()
    sys.stdout.write(text)

print("Command line arguments:")
check_args_and_parse(args[1:])

