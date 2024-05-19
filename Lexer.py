import re

def add(token_type, chr_num, token_types, code):
    token = ''
    # Iterate over characters until either the end of the code or the pattern doesn't match
    while chr_num < len(code) and re.match(token_types[token_type], code[chr_num]):
        token += code[chr_num]
        chr_num += 1
    return token, chr_num

class Lexer:
    def __init__(self, code):
        self.code = code
        # Define token types with regular expressions
        self.token_types = {
            'integer': r'(\d+)',  # Regular expression for integers
            'identifier': r'([a-zA-Z_]\w*)',  # Regular expression for identifiers
            'operator': r'([+\-*/<>&.@/:=˜|$!#%^_[\]{}"`?])',  # Additional operators
            'string': r'"((?:\\.|[^"])*)"',  # Regular expression for strings
            'comment': r"(\/\/.*)$",  # Regular expression for comments
            'punctuation': r'([\(\);,])',  # Regular expression for punctuation
            'whitespace': r'(\s+)'  # Regular expression for whitespace
        }
        # Tokenize the code
        self.tokens = self.tokenize()

    def tokenize(self):
        ini_tokens = []
        chr_num = 0  # Initialize character index
        comment_flge = False
        comment = ""
        token_types = self.token_types
        # Iterate over each character in the code
        while chr_num < len(self.code):
            char = self.code[chr_num]
            # Check for comment starting with '//'
            if char == '/':
                if chr_num + 1 < len(self.code) and self.code[chr_num + 1] == '/':
                    comment_flge = True
            # Check for end of line to reset comment flag
            elif (char == '\n' or chr_num == len(self.code)) and comment_flge == True:
                comment_flge = False
            # Skip processing if inside a comment
            if comment_flge == True:
                chr_num += 1
            else:
                # Process tokens based on token types
                for token_type, pattern in token_types.items():
                    if re.match(pattern, char):
                        token, chr_num = add(token_type, chr_num, token_types, self.code)
                        ini_tokens.append((token, token_type))
                        break
                else:
                    # If no token type matches, move to the next character
                    chr_num += 1

        return ini_tokens

# Example usage
rpal_code = """let x = 10 in
    x + 5 // This is a comment
"""
lexer = Lexer(rpal_code)
print(lexer.tokens)
