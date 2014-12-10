public class JavaVersionDisplayApplet extends java.applet.Applet
{
  private String m_ver;
  private String m_ven;

  public JavaVersionDisplayApplet() {
    m_ver = System.getProperty("java.version");
    m_ven = System.getProperty("java.vendor");
  }

  public String getVersion() {
    return m_ver;
  }

  public String getVendor() {
    return m_ven;
  }
} 
