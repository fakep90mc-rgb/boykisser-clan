#!/usr/bin/env python3
"""
Fake Virus Prank — 100% harmless, all show, no damage.
Runs a series of scary-looking screens but deletes nothing,
installs nothing, and touches no files. Run, laugh, close it.
"""

import os
import sys
import time
import random
import shutil
import ctypes
import subprocess
import tkinter as tk
from tkinter import messagebox

# ---------------------------------------------------------------------------
# OS helpers
# ---------------------------------------------------------------------------

IS_WINDOWS = sys.platform == "win32"
IS_MAC = sys.platform == "darwin"

def fake_admin():
    """Attempts visual elevation tricks. Never escalates for real.

    When frozen as an exe we keep the console window so the fake
    hacker/BSOD sequence is actually visible. As a plain .py script
    on Windows the console is hidden for extra spook factor.
    """
    if getattr(sys, "frozen", False):
        return
    try:
        if IS_WINDOWS:
            ctypes.windll.user32.ShowWindow(
                ctypes.windll.kernel32.GetConsoleWindow(), 0
            )
    except Exception:
        pass

def move_taskbar():
    """Windows-only: auto-hide the taskbar (harmless, restores on mouseover)."""
    if not IS_WINDOWS:
        return
    try:
        ctypes.windll.user32.SystemParametersInfoW(0x001C, 1, 0, 0)  # SPI_SETAUTOHIDETASKBAR
    except Exception:
        pass

def restore_taskbar():
    if not IS_WINDOWS:
        return
    try:
        ctypes.windll.user32.SystemParametersInfoW(0x001C, 0, 0, 0)
    except Exception:
        pass

# ---------------------------------------------------------------------------
# Terminal fake-hacker screen
# ---------------------------------------------------------------------------

ANSI = "\033["
GREEN = ANSI + "92m"
RED = ANSI + "91m"
YELLOW = ANSI + "93m"
CYAN = ANSI + "96m"
WHITE = ANSI + "97m"
RESET = ANSI + "0m"
BOLD = ANSI + "1m"

FAKE_LINES = [
    ("[SYS] Booting intrusion module...", GREEN),
    ("[SYS] Scanning local filesystem (fake)...", GREEN),
    ("[SYS] Exploit payload located: prank.exe", RED),
    ("[NET] Connecting to C2 server 127.0.0.1 (kidding)...", CYAN),
    ("[NET] Handshake complete. Session established.", CYAN),
    ("[CRYPT] Generating 4096-bit key ... done", YELLOW),
    ("[CRYPT] Encrypting files ... 0 files affected (it's fake)", YELLOW),
    ("[WARN] User detected. Deploying decoy...", RED),
    ("[SYS] Overriding system32 ... haha no", RED),
    ("[SYS] Deleting system32 ... JUST KIDDING", RED),
    ("[NET] Uploading your search history ... nothing sent", CYAN),
    ("[MISC] Installing Toaster firmware v9.9 ...", WHITE),
    ("[MISC] Toaster successfully bricked.", WHITE),
    ("[SYS] Finalizing pwn. You have been pranked.", GREEN),
]

def fake_hacker_screen():
    """Actor-style scrolling text then falling binary rain."""
    lines = []
    for i in range(40):
        if i < len(FAKE_LINES):
            text, color = FAKE_LINES[i]
        else:
            text = fake_line()
            color = random.choice([GREEN, CYAN, WHITE, YELLOW])
        lines.append(color + text + RESET)

    available = shutil.get_terminal_size((80, 24)).lines
    print("\n".join(" " * 0 for _ in range(max(0, available - 20))) + "\n")
    for line in lines:
        print(line)
        time.sleep(0.18)
    time.sleep(0.6)

    # binary rain
    cols = shutil.get_terminal_size((80, 24)).columns
    rows = shutil.get_terminal_size((80, 24)).lines
    stream = [[random.choice("01") for _ in range(cols)] for _ in range(rows)]
    for _ in range(12):
        sys.stdout.write("\033[H")
        for r in range(rows):
            chars = []
            for c in range(cols):
                bit = random.random()
                if bit < 0.08:
                    chars.append(GREEN + "1")
                elif bit < 0.16:
                    chars.append(WHITE + "0")
                else:
                    chars.append(RESET + random.choice("01 "))
            sys.stdout.write("".join(chars) + "\n")
        sys.stdout.flush()
        time.sleep(0.07)

def fake_line():
    bases = [
        ("[SYS] Poking process %d ...", (random.randint(1, 9999),)),
        ("[NET] Ping 192.168.%d.%d ... reply", (random.randint(0, 255), random.randint(0, 255))),
        ("[CRYPT] Chunk %d decrypted (pretend)", (random.randint(1, 9999),)),
        ("[NET] Spoofing MAC ... 0x%X", (random.randint(0, 0xFFFFFF),)),
        ("[SYS] Allocating memory ... pranked", ()),
        ("[NET] Beacon out ... 127.0.0.1:%d", (random.randint(1024, 65535),)),
        ("[SYS] Overriding clock ... +%ds", (random.randint(1, 60),)),
    ]
    fmt, args = random.choice(bases)
    return fmt % args

# ---------------------------------------------------------------------------
# Fake BSOD / fake error screens
# ---------------------------------------------------------------------------

def fake_bsod():
    """Fake BSOD. Tries a real Windows-style fullscreen dialog on
    plain .py runs; frozen exe shows the terminal version so the
    user gets the full show in the console."""
    # Scary window title on Windows exe
    if IS_WINDOWS:
        try:
            ctypes.windll.kernel32.SetConsoleTitleW("Windows Security - CRITICAL ERROR")
        except Exception:
            pass

    if IS_WINDOWS and not getattr(sys, "frozen", False):
        try:
            ctypes.windll.user32.ShowWindow(
                ctypes.windll.kernel32.GetConsoleWindow(), 0
            )
            msg = tk.Tk()
            msg.withdraw()
            messagebox.showwarning(
                "Prank Complete",
                "The 'virus' was 100% fake. Your files are fine. Breathe. :)",
            )
            msg.destroy()
            return
        except Exception:
            pass

    # Terminal fallback: fake panic
    shutil.get_terminal_size((80, 24))
    try:
        os.system("clear" if not IS_WINDOWS else "cls")
    except Exception:
        pass
    pink = ANSI + "95m"
    print(pink + "#" * 90)
    print("A problem has been detected and Windows has been shut down to prevent")
    print("damage to your computer.")
    print()
    print("PRANK_IRQ_NOT_LESS_OR_EQUAL")
    print()
    print("Technical information:")
    print()
    print("*** STOP: 0x0000000A (0x00000000, 0x00000002, 0x00000001, 0x804D3A60)")
    print()
    print("Beginning dump of physical memory. 0 percent complete.")
    for pct in range(0, 101, 10):
        sys.stdout.write("\rBeginning dump of physical memory. %d percent complete." % pct)
        sys.stdout.flush()
        time.sleep(0.15)
    print()
    print()
    print("(This is a prank. Press Ctrl+C to exit.)")
    print(RESET)
    time.sleep(4)

# ---------------------------------------------------------------------------
# Tk popup storm — creepy but harmless
# ---------------------------------------------------------------------------

POPUP_TEXTS = [
    "VIRUS DETECTED",
    "!!! SYSTEM BREACH !!!",
    "YOUR FILES ARE BEING RENAMED",
    "UPLOADING DATA ... 74%",
    "DO NOT TURN OFF YOUR COMPUTER",
    "I AM INSIDE YOUR WEBCAM",
    "PLEASE INSERT FLOPPY DISK TO CONTINUE",
    "YOUR MEMES ARE MINE NOW",
    "INSTALLING SASS (system-wide)",
    "FOUND 999 BUGS. FIXING ONE...",
    "WARNING: TOO MANY TABS OPEN",
    "REBOOTING INTO TOASTER MODE",
]

def popup_storm(seconds=6, count=6):
    root = tk.Tk()
    root.withdraw()
    windows = []
    for _ in range(count):
        win = tk.Toplevel(root)
        win.overrideredirect(True)
        w = random.randint(320, 460)
        h = random.randint(140, 220)
        sw = root.winfo_screenwidth()
        sh = root.winfo_screenheight()
        x = random.randint(0, max(1, sw - w))
        y = random.randint(0, max(1, sh - h))
        win.geometry(f"{w}x{h}+{x}+{y}")
        win.configure(bg="black")
        color = random.choice(["#ff0033", "#ff6600", "#ff00ff"])
        label = tk.Label(
            win,
            text=random.choice(POPUP_TEXTS),
            fg=color,
            bg="black",
            font=("Courier New", 14, "bold"),
        )
        label.pack(expand=True)
        win.attributes("-topmost", True)
        windows.append(win)
    root.update()
    start = time.time()
    while time.time() - start < seconds:
        for win in windows:
            try:
                win.update()
            except Exception:
                pass
        time.sleep(0.05)
    for win in windows:
        try:
            win.destroy()
        except Exception:
            pass
    root.destroy()

# ---------------------------------------------------------------------------
# Screen inverting (Windows/macOS where allowed) — visual only
# ---------------------------------------------------------------------------

def flip_screen():
    """Rotates display briefly on Windows. Restores angle to 0. Best-effort."""
    if not IS_WINDOWS:
        return
    try:
        subprocess.Popen(
            ["rundll32.exe", "user32.dll,LockWorkStation"],  # locks screen (harmless)
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
    except Exception:
        pass

# ---------------------------------------------------------------------------
# Final reveal
# ---------------------------------------------------------------------------

def reveal():
    try:
        os.system("clear" if not IS_WINDOWS else "cls")
    except Exception:
        pass
    box_w = 56
    art = [
        "   _____  __  __  _   _ _  __ ",
        "  |  __ \\|  \\/  | \\ \\ / / |/ / ",
        "  | |__) | \\  / |  \\ V /| ' /   ",
        "  |  _  /| |\\/| |   > < |  <   ",
        "  | | \\ \\| |  | |  / . \\| . \\   ",
        "  |_|  \\_\\_|  |_| /_/ \\_\\_|\\_\\  ",
    ]
    print(BOLD + GREEN + "=" * box_w + RESET)
    for line in art:
        print(BOLD + GREEN + line + RESET)
    print()
    print(BOLD + WHITE + "   NICE TRY, BUT ALL OF THAT WAS 100% FAKE." + RESET)
    print(BOLD + WHITE + "   Nothing was deleted. Nothing was hacked." + RESET)
    print(BOLD + WHITE + "   No webcam, no files, no toaster. Just a prank." + RESET)
    print()
    print(BOLD + YELLOW + "   Close this window and go about your day. :)" + RESET)
    print()
    print(BOLD + GREEN + "=" * box_w + RESET)

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    fake_admin()
    try:
        move_taskbar()
    except Exception:
        pass

    print(BOLD + GREEN + "[+] Fake virus engaged. All effects are visual only." + RESET)
    time.sleep(1.2)

    try:
        popup_storm(seconds=5, count=5)
    except Exception:
        pass

    fake_hacker_screen()

    try:
        popup_storm(seconds=4, count=3)
    except Exception:
        pass

    fake_bsod()

    restore_taskbar()
    reveal()

    try:
        input(BOLD + WHITE + "\n[ Press ENTER to close ]" + RESET)
    except EOFError:
        pass

if __name__ == "__main__":
    main()