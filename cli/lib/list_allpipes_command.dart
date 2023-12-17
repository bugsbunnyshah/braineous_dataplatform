import 'dart:convert';

import 'package:enough_ascii_art/enough_ascii_art.dart' as art;
import 'package:http/http.dart' as http;

class ListAllPipesCommand {

  Future<String> execute(List<dynamic> arguments) async {
    String message = "";

    //
    RestInvocationResponse invocationResponse = await invokeRestEndpoint(arguments);
    message += '${invocationResponse.json}';

    var unicode = art.renderUnicode(message, art.UnicodeFont.doublestruck);

    //return unicode.toString();

    return message;
  }
}

Future<RestInvocationResponse> invokeRestEndpoint(List<dynamic> arguments) async {
  final url = Uri.http('localhost:8080', '/pipeline_manager/all_pipes/');

  final response = await http.get(url,
    headers: {
      "x-api-key":arguments[1],
      "x-api-key-secret": arguments[2],
    },);

  // If the request didn't succeed, throw an exception
  if (response.statusCode != 200) {
    throw RestInvocationException(
      statusCode: response.statusCode,
    );
  }

  final responseJson = jsonDecode(response.body) as List<dynamic>;

  return RestInvocationResponse.fromJson(responseJson);
}

class RestInvocationResponse {
  final List<dynamic> json;

  RestInvocationResponse({
    required this.json,
  });

  factory RestInvocationResponse.fromJson(List<dynamic> json) {
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