import requests
from bs4 import BeautifulSoup
import os
from PIL import Image
from io import BytesIO

tuples = [("https://play.pokemonshowdown.com/sprites/bw/", "normal", "sprites_"),
("https://play.pokemonshowdown.com/sprites/bw-shiny/", "shiny", "sprshiny_"),
]


for tuple in tuples:
    (url,dir,prefix) = tuple
    if os.path.isdir(dir) == False:
        os.mkdir(dir)
    
    r = requests.get(url)
    soup = BeautifulSoup(r.text, 'html.parser')
    links = soup.findAll("a")
    for link in links:
        if ".png" in link.text:
            r = requests.get(url + link.text)
            i = Image.open(BytesIO(r.content))
            i.save(dir + "/" + prefix + link.text.replace("-","").replace(" ","").replace("_",""))
            print("Downloaded "  + prefix  + link.text.replace("-","").replace(" ","").replace("_",""))


