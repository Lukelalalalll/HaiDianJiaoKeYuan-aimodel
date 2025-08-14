package com.zklcsoftware.basic.util.io;

import java.io.IOException;
import java.io.InputStream;

public abstract interface IInputStreamSource {
  public abstract InputStream getInputStream()
    throws IOException;
}
