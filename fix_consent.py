import sys

def modify(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    content = content.replace("import com.google.android.gms.ads.MobileAds\n", "")
    content = content.replace("import java.util.concurrent.atomic.AtomicBoolean\n", "")
    content = content.replace("    private var isMobileAdsInitializeCalled = AtomicBoolean(false)\n", "")
    
    # Replace initializeMobileAdsSdk() call
    content = content.replace("            initializeMobileAdsSdk()\n", "")
    
    # Remove initializeMobileAdsSdk method
    method_to_remove = """    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        MobileAds.initialize(activity) {}
    }"""
    content = content.replace(method_to_remove, "")

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == '__main__':
    modify('app/src/main/java/com/example/ads/ConsentManager.kt')
