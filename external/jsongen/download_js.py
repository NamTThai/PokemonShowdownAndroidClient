import requests

file_urls = ["https://raw.githubusercontent.com/Zarel/Pokemon-Showdown/master/data/abilities.js",
         "https://raw.githubusercontent.com/Zarel/Pokemon-Showdown/master/data/formats-data.js",
         "https://raw.githubusercontent.com/Zarel/Pokemon-Showdown/master/data/items.js",
         "https://raw.githubusercontent.com/Zarel/Pokemon-Showdown/master/data/learnsets.js",
         "https://raw.githubusercontent.com/Zarel/Pokemon-Showdown/master/data/moves.js",
         "https://raw.githubusercontent.com/Zarel/Pokemon-Showdown/master/data/pokedex.js"
        ]
        
for file in file_urls:
    print(file)
    filename = file.split('/')[-1].split('.')[0]
    filename_with_ext = file.split('/')[-1]
    r = requests.get(file)
    output = r.text
    output = "var " + filename.replace("-","_") + " = " + output[output.find("{"):]
    f = open(filename_with_ext, 'w')
    f.write(output)
    f.close()
    
   
