import urllib.request
# Download a real mp3 for background music
try:
    urllib.request.urlretrieve("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "app/src/main/res/raw/background_music.mp3")
except Exception as e:
    pass
