import 'package:cli/move_pipe_to_deployed_command.dart';
import 'package:cli/move_pipe_to_dev_command.dart';

import 'list_allpipes_command.dart';
import 'move_pipe_to_staging_command.dart';

class CommandRegistry{
  static CommandRegistry registry = CommandRegistry();

  Map<String,dynamic> commands = {};

  CommandRegistry(){
    commands['show pipes'] = ListAllPipesCommand();
    commands['move pipe_to_development'] = MovePipeToDevCommand();
    commands['move pipe_to_staging'] = MovePipeToStagingCommand();
    commands['move pipe_to_production'] = MovePipeToDeployedCommand();
  }
}