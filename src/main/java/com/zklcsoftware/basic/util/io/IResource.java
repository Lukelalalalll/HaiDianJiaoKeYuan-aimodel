package com.zklcsoftware.basic.util.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public abstract interface IResource extends IInputStreamSource {
  public abstract boolean exists();

  public abstract boolean isOpen();

  public abstract URL getURL()
    throws IOException;

  public abstract File getFile()
    throws IOException;

  public abstract String getDescription();
}
