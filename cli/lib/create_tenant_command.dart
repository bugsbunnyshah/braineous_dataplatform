import 'dart:convert';

import 'package:enough_ascii_art/enough_ascii_art.dart' as art;
import 'package:http/http.dart' as http;

class CreateTenantCommand {

  Future<Map<String,dynamic>> execute(List<dynamic> arguments) async {
    RestInvocationResponse invocationResponse = await invokeRestEndpoint(arguments);
    return invocationResponse.json;
  }
}

Future<RestInvocationResponse> invokeRestEndpoint(List<dynamic> arguments) async {
  final url = Uri.http('localhost:8080', '/tenant_manager/create_tenant/');

  Map<String,dynamic> jsonMap = {};
  jsonMap['name'] = arguments[0];
  jsonMap['email'] = arguments[1];
  String json = jsonEncode(jsonMap);

  final response = await http.post(url,
      body: json);

  // If the request didn't succeed, throw an exception
  if (response.statusCode != 200) {
    throw RestInvocationException(
      statusCode: response.statusCode,
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

  RestInvocationException({this.statusCode});

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