import os
import re
import sys
import json
import hashlib
import shutil
import subprocess
from datetime import datetime, timezone

def run_cmd(cmd, cwd=None, check=True):
    print(f"Running: {cmd}")
    env = os.environ.copy()
    env["JAVA_HOME"] = r"C:\Program Files\Android\Android Studio1\jbr"
    env["PATH"] = os.path.join(env["JAVA_HOME"], "bin") + os.pathsep + env.get("PATH", "")

    res = subprocess.run(cmd, shell=True, cwd=cwd, env=env, capture_output=True, text=True)
    if check and res.returncode != 0:
        print(f"Error executing {cmd}:\nSTDOUT: {res.stdout}\nSTDERR: {res.stderr}")
        sys.exit(res.returncode)
    return res

def publish_build(release_notes):
    launcher_dir = r"C:\Users\Marcos Calvin Dudang\Desktop\Slyvos\slyvos-launcher"
    updates_dir = r"C:\Users\Marcos Calvin Dudang\Desktop\Slyvos\slyvos-launcher-updates"
    gradle_file = os.path.join(launcher_dir, "app", "build.gradle.kts")

    print("\n=====================================================")
    print(" SLYVOS AUTOMATED DEVELOPMENT PUBLISHING WORKFLOW")
    print("=====================================================")

    # 1. Run Unit Tests
    print("\n[1/6] Running Unit Tests...")
    run_cmd(r".\gradlew.bat test", cwd=launcher_dir)
    print("[SUCCESS] All unit tests passed successfully.")

    # Sync updates repo first
    run_cmd("git fetch origin", cwd=updates_dir, check=False)
    run_cmd("git reset --hard origin/main", cwd=updates_dir, check=False)

    # 2. Parse & Increment Build Number
    print("\n[2/6] Parsing current build number from app/build.gradle.kts...")
    with open(gradle_file, "r", encoding="utf-8") as f:
        content = f.read()

    match = re.search(r'buildConfigField\("int",\s*"BUILD_NUMBER",\s*"(\d+)"\)', content)
    if not match:
        print("[ERROR] Could not parse BUILD_NUMBER from build.gradle.kts")
        sys.exit(1)

    current_build_num = int(match.group(1))
    new_build_num = current_build_num + 1
    new_version_code = new_build_num
    formatted_build_num = f"{new_build_num:03d}"
    new_version_name = f"Pre-Alpha Build #{formatted_build_num}"

    print(f"Upgrading Build Number: #{current_build_num:03d} -> #{formatted_build_num}")

    content = re.sub(r'versionCode = \d+', f'versionCode = {new_version_code}', content)
    content = re.sub(r'versionName = "[^"]+"', f'versionName = "{new_version_name}"', content)
    content = re.sub(r'buildConfigField\("int",\s*"BUILD_NUMBER",\s*"\d+"\)', f'buildConfigField("int", "BUILD_NUMBER", "{new_build_num}")', content)

    with open(gradle_file, "w", encoding="utf-8") as f:
        f.write(content)

    # 3. Assemble Debug APK
    print(f"\n[3/6] Assembling Debug APK for Build #{formatted_build_num}...")
    run_cmd(r".\gradlew.bat assembleDebug", cwd=launcher_dir)
    print("[SUCCESS] APK assembled successfully.")

    # 4. Calculate Checksum & Copy to updates repo
    apk_source = os.path.join(launcher_dir, "app", "build", "outputs", "apk", "debug", "app-debug.apk")
    if not os.path.exists(apk_source):
        print(f"[ERROR] APK not found at {apk_source}")
        sys.exit(1)

    apk_filename = f"slyvos-prealpha-{formatted_build_num}.apk"
    apk_dest = os.path.join(updates_dir, apk_filename)
    shutil.copyfile(apk_source, apk_dest)

    sha256 = hashlib.sha256(open(apk_dest, "rb").read()).hexdigest()
    print(f"[SUCCESS] SHA-256 Checksum: {sha256}")

    # 5. Update pre-alpha.json manifest
    print("\n[4/6] Updating pre-alpha.json manifest...")
    raw_apk_url = f"https://raw.githubusercontent.com/ryo-builds/slyvos-launcher-updates/main/{apk_filename}"
    manifest_file = os.path.join(updates_dir, "pre-alpha.json")

    manifest_data = {
        "buildNumber": new_build_num,
        "versionCode": new_version_code,
        "versionName": new_version_name,
        "releaseStage": "PRE_ALPHA",
        "releaseNotes": release_notes,
        "apkUrl": raw_apk_url,
        "apkSha256": sha256,
        "publishedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "minimumSupportedBuildNumber": 1
    }

    with open(manifest_file, "w", encoding="utf-8") as f:
        json.dump(manifest_data, f, indent=2)
    print("[SUCCESS] Manifest pre-alpha.json updated.")

    # 6. Commit & Push slyvos-launcher-updates
    print("\n[5/6] Pushing update release to slyvos-launcher-updates...")
    run_cmd("git add .", cwd=updates_dir)
    run_cmd(f'git commit -m "Release Slyvos Pre-Alpha Build #{formatted_build_num}"', cwd=updates_dir)
    run_cmd("git push origin main", cwd=updates_dir)
    print("[SUCCESS] Pushed update files to GitHub slyvos-launcher-updates.")

    # 7. Commit & Push slyvos-launcher
    print("\n[6/6] Committing & pushing codebase to slyvos-launcher...")
    run_cmd("git add .", cwd=launcher_dir)
    run_cmd(f'git commit -m "Build #{formatted_build_num}: {release_notes}"', cwd=launcher_dir)
    run_cmd("git push origin main", cwd=launcher_dir)
    print("[SUCCESS] Pushed codebase to GitHub slyvos-launcher.")

    print("\n=====================================================")
    print(f" BUILD #{formatted_build_num} PUBLISHED SUCCESSFULLY!")
    print("=====================================================")
    print(f"Identity: Slyvos Pre-Alpha Build #{formatted_build_num}")
    print("Manifest URL: https://raw.githubusercontent.com/ryo-builds/slyvos-launcher-updates/main/pre-alpha.json")
    print(f"APK URL: {raw_apk_url}")
    print(f"SHA-256: {sha256}")

if __name__ == "__main__":
    notes = sys.argv[1] if len(sys.argv) > 1 else "Slyvos Pre-Alpha Development Build Update"
    publish_build(notes)
