Add-Type @"
using System;
using System.Runtime.InteropServices;

public class ProcessLauncher {
    [StructLayout(LayoutKind.Sequential)]
    public struct STARTUPINFO {
        public int cb;
        public string lpReserved;
        public string lpDesktop;
        public string lpTitle;
        public int dwX;
        public int dwY;
        public int dwXSize;
        public int dwYSize;
        public int dwXCountChars;
        public int dwYCountChars;
        public int dwFillAttribute;
        public int dwFlags;
        public short wShowWindow;
        public short cbReserved2;
        public IntPtr lpReserved2;
        public IntPtr hStdInput;
        public IntPtr hStdOutput;
        public IntPtr hStdError;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct PROCESS_INFORMATION {
        public IntPtr hProcess;
        public IntPtr hThread;
        public int dwProcessId;
        public int dwThreadId;
    }

    [DllImport("kernel32.dll", SetLastError = true, CharSet = CharSet.Auto)]
    public static extern bool CreateProcess(
        string lpApplicationName,
        string lpCommandLine,
        IntPtr lpProcessAttributes,
        IntPtr lpThreadAttributes,
        bool bInheritHandles,
        uint dwCreationFlags,
        IntPtr lpEnvironment,
        string lpCurrentDirectory,
        ref STARTUPINFO lpStartupInfo,
        out PROCESS_INFORMATION lpProcessInformation
    );
}
"@

$si = New-Object ProcessLauncher+STARTUPINFO
$si.cb = [System.Runtime.InteropServices.Marshal]::SizeOf($si)
$si.lpDesktop = "Default"

$pi = New-Object ProcessLauncher+PROCESS_INFORMATION

$app = $args[0]
$cmd = if ($args.Length -gt 1) { $args[1] } else { $null }

# 0x00000010 = CREATE_NEW_CONSOLE
$success = [ProcessLauncher]::CreateProcess(
    $app,
    $cmd,
    [IntPtr]::Zero,
    [IntPtr]::Zero,
    $false,
    0x00000010,
    [IntPtr]::Zero,
    $pwd.Path,
    [ref]$si,
    [ref]$pi
)

Write-Output "CreateProcess success=$success, PID=$($pi.dwProcessId), Error=$([System.Runtime.InteropServices.Marshal]::GetLastWin32Error())"
