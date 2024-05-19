import re

class Lexer:
    def __init__(self, code):
        self.code = code
        self.token_types = {
            'integer': r'(\d+)',  # Regular expression for integers
            'identifier': r'([a-zA-Z_]\w*)',  # Regular expression for identifiers
            'operator': r'([+\-*/<>&.@/:=˜|$!#%^_[\]{}"`?])',  # Additional operators
            'string': r'"((?:\\.|[^"])*)"',  # Regular expression for strings
            'comment': r"(\/\/.*)$",  # Regular expression for comments
            'punctuation': r'([\(\);,])',  # Regular expression for punctuation
            'whitespace': r'(\s+)'  # Regular expression for whitespace
        }
        self.tokens = self.tokenize()
        self.line_number = 1
        self.char_num = 0
        self.errors = []
        self.current_state = 'normal'
        self.lex_buffer = ''
        self.comment_flag= False

    def tokenize(self):
        tokens = []
        token = ""
        ini_tokens = []
        chr_num = 0
        comment_flge = False
        comment = ""
        for char in code:
            chr_num += 1
            if char == ' ' and comment_flge != True:
                ini_tokens.append(token)
                token = ""
            elif chr_num == len(code) and comment_flge != True:
                token += char
                ini_tokens.append(token)
                token = ""
            elif char == '/' and code[chr_num] == '/':
                comment_flge = True
                chr_num += 1
            elif char == '\n' or chr_num == len(code):
                comment = ""
                comment_flge = False
            else:
                if comment_flge != True:
                    token += char
                elif char != '/':
                    comment += char
        for word in ini_tokens:
            for token_type in self.token_types:
                pattern = self.token_types[token_type]
                match = re.match(pattern, word)
                if match:
                    token = match.group(0)
                    tokens.append((token_type, token))
                    break  # Break the inner loop if a match is found

        return tokens

    def report_error(self, message):
        self.errors.append((self.line_number, self.column_number, message))

    def update_position(self, matched_string):
        return



# Update position based on the length of the matched string
# Handle newline characters to update line_number and reset column_number

# Example usage
rpal_code = """
let x = 10 in
    x + 5 // This is a comment
"""
code = "3 + 4 * (20 - 1)"
lexer = Lexer(rpal_code)
print(lexer.tokens)
