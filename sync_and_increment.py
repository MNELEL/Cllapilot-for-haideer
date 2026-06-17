#!/usr/bin/env python3
import os
import re
import json
import argparse

def main():
    parser = argparse.ArgumentParser(description="Synchronize and increment Android app version/applicationId.")
    parser.add_argument("--increment", "-i", action="store_true", help="Automatically increment the versionCode by 1.")
    parser.add_argument("--version-code", "-vc", type=int, help="Manually set a specific versionCode.")
    parser.add_argument("--version-name", "-vn", type=str, help="Manually set a specific versionName.")
    parser.add_argument("--app-id", "-id", type=str, help="Manually set the applicationId.")
    
    args = parser.parse_args()

    gradle_path = "app/build.gradle.kts"
    manifest_path = "app/src/main/AndroidManifest.xml"
    metadata_path = "play_store_metadata.json"

    # 1. Read build.gradle.kts
    if not os.path.exists(gradle_path):
        print(f"[-] Error: Could not find {gradle_path}")
        return

    with open(gradle_path, 'r', encoding='utf-8') as f:
        gradle_content = f.read()

    # Find matches
    app_id_match = re.search(r'applicationId\s*=\s*"([^"]+)"', gradle_content)
    version_code_match = re.search(r'versionCode\s*=\s*(\d+)', gradle_content)
    version_name_match = re.search(r'versionName\s*=\s*"([^"]+)"', gradle_content)

    if not app_id_match or not version_code_match or not version_name_match:
        print("[-] Error: Failed to parse build.gradle.kts variables.")
        return

    current_app_id = app_id_match.group(1)
    current_version_code = int(version_code_match.group(1))
    current_version_name = version_name_match.group(1)

    print(f"[+] Current loaded values from {gradle_path}:")
    print(f"    - applicationId: {current_app_id}")
    print(f"    - versionCode:   {current_version_code}")
    print(f"    - versionName:   {current_version_name}")

    # Process changes
    new_app_id = args.app_id if args.app_id else current_app_id
    new_version_code = current_version_code
    
    if args.version_code is not None:
        new_version_code = args.version_code
    elif args.increment:
        new_version_code = current_version_code + 1

    if args.version_name:
        new_version_name = args.version_name
    elif args.increment or args.version_code is not None:
        new_version_name = f"{new_version_code}.0"
    else:
        new_version_name = current_version_name

    print(f"\n[+] Determined target values:")
    print(f"    - applicationId: {new_app_id}")
    print(f"    - versionCode:   {new_version_code}")
    print(f"    - versionName:   {new_version_name}")

    # Write back to build.gradle.kts
    updated_gradle = re.sub(
        r'applicationId\s*=\s*"[^"]+"', 
        f'applicationId = "{new_app_id}"', 
        gradle_content
    )
    updated_gradle = re.sub(
        r'versionCode\s*=\s*\d+', 
        f'versionCode = {new_version_code}', 
        updated_gradle
    )
    updated_gradle = re.sub(
        r'versionName\s*=\s*"[^"]+"', 
        f'versionName = "{new_version_name}"', 
        updated_gradle
    )

    with open(gradle_path, 'w', encoding='utf-8') as f:
        f.write(updated_gradle)
    print(f"[✓] Successfully updated {gradle_path}")

    # 2. Write to AndroidManifest.xml
    if os.path.exists(manifest_path):
        with open(manifest_path, 'r', encoding='utf-8') as f:
            manifest_content = f.read()

        # Sync the app ID inside <meta-data android:name="com.aistudio.play.APPLICATION_ID" android:value="..." />
        updated_manifest = re.sub(
            r'(<meta-data\s+android:name="com\.aistudio\.play\.APPLICATION_ID"\s+android:value=")[^"]+("\s*/>)',
            f'\\1{new_app_id}\\2',
            manifest_content
        )

        with open(manifest_path, 'w', encoding='utf-8') as f:
            f.write(updated_manifest)
        print(f"[✓] Successfully updated {manifest_path}")

    # 3. Write to play_store_metadata.json
    if os.path.exists(metadata_path):
        with open(metadata_path, 'r', encoding='utf-8') as f:
            try:
                meta = json.load(f)
            except Exception as e:
                meta = {}
                print(f"[-] Warning: Failed to parse {metadata_path} as JSON. Rewriting...")

        meta["applicationId"] = new_app_id
        # Also let's save versioning into metadata for extra safety
        meta["versionCode"] = new_version_code
        meta["versionName"] = new_version_name

        with open(metadata_path, 'w', encoding='utf-8') as f:
            json.dump(meta, f, indent=2, ensure_ascii=False)
        print(f"[✓] Successfully updated {metadata_path}")

if __name__ == "__main__":
    main()
