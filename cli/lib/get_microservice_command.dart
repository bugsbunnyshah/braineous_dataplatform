import 'dart:convert';

import 'package:enough_ascii_art/enough_ascii_art.dart' as art;
import 'package:http/http.dart' as http;

class GetMicroserviceCommand {

  Future<String> execute(List<dynamic> arguments) async {
    String message = arguments.toString();

    //
    RestInvocationResponse invocationResponse = await invokeRestEndpoint();
    message += '\n Product ${invocationResponse.product} :';
    message += '\n Oid ${invocationResponse.oid} :';
    message += '\n Message ${invocationResponse.message} :';

    var unicode = art.renderUnicode(message, art.UnicodeFont.doublestruck);

    return unicode.toString();
  }
}

Future<RestInvocationResponse> invokeRestEndpoint() async {
  final url = Uri.http('localhost:8080', '/pipeline_manager/move_to_development/');

  Map<String,dynamic> jsonMap = new Map();
  jsonMap['pipeId'] = "12345678";
  jsonMap['subscriptionId'] = "87654321";
  jsonMap['pipeName'] = "flights";
  String json = jsonEncode(jsonMap);

  final response = await http.post(url,body: json);

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
  final String product;
  final String oid;
  final String message;

  RestInvocationResponse({
    required this.product,
    required this.oid,
    required this.message,
  });

  factory RestInvocationResponse.fromJson(Map<String, dynamic> json) {
    return RestInvocationResponse(
      product: json['product'] as String,
      oid: json['oid'] as String,
      message: json['message'] as String,
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