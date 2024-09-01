# Baremaps GDAL

This module provides a set of utilities to interact with GDAL in a Java-friendly and idiomatic way.
It uses the official Java bindings provided by GDAL and hides some of the complexity of low-level API.
For instance, it wraps the classes, such as `Dataset` in an `AutoCloseable` interface to ensure that resources allocated by GDAL are properly released.
In order to use this module, you need to install the GDAL native libraries for Java (`libgdalalljni.so` / `libgdalalljni.dylib` / `gdalalljni.dll`) on your system.