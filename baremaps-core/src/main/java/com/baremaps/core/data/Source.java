package com.baremaps.core.data;

public class Source {

  private String id;

  private String format;

  private String archive;

  private String url;

  private String file;

  public Source() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getArchive() {
    return archive;
  }

  public void setArchive(String archive) {
    this.archive = archive;
  }
}
