# Contacts
Ejemplo de aplicación que lista contactos del teléfono. Desde la versión 6 de Gradle, no sirve solicitar los permisos de escritura, lectura, internet, etc en el AndroidManifest.xml. Además hay que hacerlo programáticamente en tiempo de ejecución.
Esta aplicación sirve para describir cómo hacerlo. En este caso, la solicitud de permisos se hace para acceder a los contactos mediante el ContentResolver.
