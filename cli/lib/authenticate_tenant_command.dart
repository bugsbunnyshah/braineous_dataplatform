import 'dart:convert';
import 'dart:io';

import 'package:cli/session/session.dart';
import 'package:enough_ascii_art/enough_ascii_art.dart' as art;
import 'package:http/http.dart' as http;

class AuthenticateTenantCommand {

  Future<void> execute(List<dynamic> arguments) async {
    try {
      Session session = Session.session;

      RestInvocationResponse invocationResponse = await invokeRestEndpoint(
          arguments);

      Map<String,dynamic> result = invocationResponse.json;
      String apiKey = result['apiKey'];
      String apiSecret = result['apiSecret'];
      session.apiKey = apiKey;
      session.apiSecret = apiSecret;
    }on RestInvocationException catch (_, e){
      print(_.json);

      Session session = Session.session;
      AuthenticateTenantCommand command = AuthenticateTenantCommand();
      var arguments = [];

      print("Login");
      print("> email: ");
      var email = stdin.readLineSync(encoding: utf8);
      session.email = email!;

      print("> password: ");
      var password = stdin.readLineSync(encoding: utf8);

      arguments.add(email);
      arguments.add(password);

      //login
      await command.execute(arguments);
    }
  }
}

Future<RestInvocationResponse> invokeRestEndpoint(List<dynamic> arguments) async {
  Session session = Session.session;
  String host = session.host;
  String port = session.port;
  String hostUrl = "$host:$port";
  final url = Uri.http(hostUrl, '/tenant_manager/authenticate_tenant/');

  Map<String,dynamic> jsonMap = {};
  jsonMap['email'] = arguments[0];
  jsonMap['password'] = arguments[1];
  String json = jsonEncode(jsonMap);

  final response = await http.post(url,
      body: json);

  // If the request didn't succeed, throw an exception
  if (response.statusCode != 200) {
    Map<String,dynamic> responseJsonMap = {};
    if(response.body.trim() != "") {
      responseJsonMap = jsonDecode(response.body) as Map<String, dynamic>;
    }

    responseJsonMap["statusCode"] = response.statusCode;
    throw RestInvocationException(
      statusCode: response.statusCode, responseJsonMap
    );
  }

  final responseJson = jsonDecode(response.body) as Map<String, dynamic>;

  return RestInvocationResponse.fromJson(responseJson);
}

class RestInvocationResponse {
  final dynamic json;

  RestInvocationResponse({
    required this.json,
  });

  factory RestInvocationResponse.fromJson(Map<String, dynamic> json) {
    return RestInvocationResponse(
      json: json,
    );
  }
}

class RestInvocationException implements Exception {
  final int? statusCode;
  final dynamic json;

  RestInvocationException(this.json, {this.statusCode});

  @override
  String toString() {
    final buf = StringBuffer();
    buf.write('Failed to invoke endpoint');

    if (statusCode != null) {
      buf.write(' with a status code of $statusCode');
    }

    buf.write('!');
    return buf.toString();
  }
}