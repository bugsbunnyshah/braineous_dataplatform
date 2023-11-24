import 'dart:convert';

import 'package:enough_ascii_art/enough_ascii_art.dart' as art;
import 'package:cli/get_microservice_command.dart';

void main() async {
  String message = "********************\n BRAINEOUS \n *****************";
  var unicode = art.renderUnicode(message, art.UnicodeFont.doublestruck);
  print(unicode);

  //execute command
  var arguments = [];
  arguments.add("do_this_0");
  arguments.add("do_this_1");
  GetMicroserviceCommand command = GetMicroserviceCommand();
  String executionMessage = await command.execute(arguments);
  print(executionMessage);
}