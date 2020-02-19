package com.github.unidbg.ios;

import com.github.unidbg.arm.AbstractARMEmulator;
import com.github.unidbg.file.FileSystem;
import com.github.unidbg.file.ios.DarwinFileSystem;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.memory.SvcMemory;
import com.github.unidbg.pointer.UnicornPointer;
import com.github.unidbg.spi.Dlfcn;
import com.github.unidbg.spi.LibraryFile;
import com.github.unidbg.unix.UnixSyscallHandler;
import com.sun.jna.Pointer;

import java.io.File;
import java.net.URL;
import java.util.Collections;

public class DarwinARMEmulator extends AbstractARMEmulator {

    public DarwinARMEmulator() {
        this(null, null);
    }

    public DarwinARMEmulator(String processName) {
        this(processName, null);
    }

    public DarwinARMEmulator(File rootDir) {
        this(null, rootDir);
    }

    public DarwinARMEmulator(String processName, File rootDir, String... envs) {
        super(processName, rootDir, envs);
    }

    @Override
    protected FileSystem createFileSystem(File rootDir) {
        return new DarwinFileSystem(this, rootDir);
    }

    @Override
    protected void setupTraps() {
        super.setupTraps();

        long _COMM_PAGE_MEMORY_SIZE = (MachO._COMM_PAGE32_BASE_ADDRESS+0x038);	// uint64_t max memory size */
        Pointer commPageMemorySize = UnicornPointer.pointer(this, _COMM_PAGE_MEMORY_SIZE);
        if (commPageMemorySize != null) {
            commPageMemorySize.setLong(0, 0);
        }

        long _COMM_PAGE_NCPUS = (MachO._COMM_PAGE32_BASE_ADDRESS+0x022);	// uint8_t number of configured CPUs
        Pointer commPageNCpus = UnicornPointer.pointer(this, _COMM_PAGE_NCPUS);
        if (commPageNCpus != null) {
            commPageNCpus.setByte(0, (byte) 1);
        }

        long _COMM_PAGE_ACTIVE_CPUS = (MachO._COMM_PAGE32_BASE_ADDRESS+0x034);	// uint8_t number of active CPUs (hw.activecpu)
        Pointer commPageActiveCpus = UnicornPointer.pointer(this, _COMM_PAGE_ACTIVE_CPUS);
        if (commPageActiveCpus != null) {
            commPageActiveCpus.setByte(0, (byte) 1);
        }

        long _COMM_PAGE_PHYSICAL_CPUS = (MachO._COMM_PAGE32_BASE_ADDRESS+0x035);	// uint8_t number of physical CPUs (hw.physicalcpu_max)
        Pointer commPagePhysicalCpus = UnicornPointer.pointer(this, _COMM_PAGE_PHYSICAL_CPUS);
        if (commPagePhysicalCpus != null) {
            commPagePhysicalCpus.setByte(0, (byte) 1);
        }

        long _COMM_PAGE_LOGICAL_CPUS = (MachO._COMM_PAGE32_BASE_ADDRESS+0x036);	// uint8_t number of logical CPUs (hw.logicalcpu_max)
        Pointer commPageLogicalCpus = UnicornPointer.pointer(this, _COMM_PAGE_LOGICAL_CPUS);
        if (commPageLogicalCpus != null) {
            commPageLogicalCpus.setByte(0, (byte) 1);
        }
    }

    @Override
    protected Memory createMemory(UnixSyscallHandler syscallHandler, String[] envs) {
        return new MachOLoader(this, syscallHandler, envs);
    }

    @Override
    protected Dlfcn createDyld(SvcMemory svcMemory) {
        return new Dyld32((MachOLoader) memory, svcMemory);
    }

    @Override
    protected UnixSyscallHandler createSyscallHandler(SvcMemory svcMemory) {
        return new ARM32SyscallHandler(svcMemory);
    }

    @Override
    public String getLibraryExtension() {
        return ".dylib";
    }

    @Override
    public String getLibraryPath() {
        return "/ios/lib/";
    }

    @Override
    public LibraryFile createURLibraryFile(URL url, String libName) {
        return new URLibraryFile(url, libName, null, Collections.<String>emptyList());
    }

    @Override
    protected boolean isPaddingArgument() {
        return false;
    }
}
