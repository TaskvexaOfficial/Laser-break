import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    target = """    val canRequestAds by consentManager.canRequestAds.collectAsState()
    LaunchedEffect(canRequestAds) {
        if (canRequestAds) {
            rewardedAdManager.loadAd()
        }
    }"""
    
    content = content.replace(target, """    val canRequestAds by consentManager.canRequestAds.collectAsState()""")

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/java/com/example/ui/AppNavigation.kt')
