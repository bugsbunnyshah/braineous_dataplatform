import 'dart:convert';

import 'package:cli/create_subscription_command.dart';
import 'package:enough_ascii_art/enough_ascii_art.dart' as art;
import 'package:cli/move_pipe_to_dev_command.dart';

void main() async {
  String message = "********************\n BRAINEOUS \n *****************";
  var unicode = art.renderUnicode(message, art.UnicodeFont.doublestruck);
  print(unicode);

  //execute command
  var arguments = [];
  arguments.add("books");
  MovePipeToDevCommand movePipeToDevCommand = MovePipeToDevCommand();
  String executionMessage = await movePipeToDevCommand.execute(arguments);
  print(executionMessage);
}