import 'list_allpipes_command.dart';

class CommandRegistry{
  static CommandRegistry registry = CommandRegistry();

  Map<String,dynamic> commands = {};

  CommandRegistry(){
    commands['show pipes'] = ListAllPipesCommand();
  }
}