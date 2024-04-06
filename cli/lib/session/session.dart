class Session{
  static Session session = Session();

  String host = "";
  String port = "";
  String tenant = "";
  String email = "";
  String apiKey = "";
  String apiSecret = "";
  String usingPipe = "";

  @override
  String toString() {
    return 'Session{host: $host, port: $port, tenant: $tenant, username: $email}';
  }
}