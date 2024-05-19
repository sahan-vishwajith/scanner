import re
code= "3 + 4 * (20 - 1)"
token=""
ini_tokens=[]
chr_num=0
comment_flge=False
comment=""
for char in code:
    chr_num+=1
    if char==' ' and comment_flge!= True :
        ini_tokens.append(token)
        token=""
    elif chr_num==len(code) and comment_flge!= True:
        token += char
        ini_tokens.append(token)
        token = ""
    elif char=='/' and code[chr_num]== '/':
        comment_flge=True
        chr_num+=1
    elif char=='\n' or chr_num==len(code):
        comment=""
        comment_flge=False
    else:
        if comment_flge!= True:
            token += char
        elif char!='/':
            comment+=char

# pattern = re.ciompile(r'([a-zA-Z_]\w*)')
# match = re.match(pattern, string)
# if match:
#     print("Match found:", match.group())
# match = re.match(pattern, string)
# if match:
#     print("Match found:", match.group())
#
# else:
#     print("not found error")
print(ini_tokens)