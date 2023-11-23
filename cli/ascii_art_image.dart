import 'package:ascii_art_tree/ascii_art_tree.dart';

void main() async {
  var paths = [
    '/workspace/project-x/src/base.dart',
    '/workspace/project-x/src/entities.dart',
    '/workspace/project-x/src/system.dart',
    '/workspace/project-x/assets/file2.txt',
    '/workspace/project-x/assets/images/imgX.png',
    '/workspace/project-x/assets/images/imgY.png',
  ];

  var asciiArtTree = ASCIIArtTree.fromStringPaths(
    paths,
    style: ASCIIArtTreeStyle.elegant,
  );

  var tree = asciiArtTree.generate();
  print(tree);
}

/////////////
// OUTPUT: //
/////////////
// /
// └─┬─ workspace
//   └─┬─ project-x
//     ├─┬─ src
//     │ ├──> base.dart
//     │ ├──> entities.dart
//     │ └──> system.dart
//     └─┬─ assets
//       ├──> file2.txt
//       └─┬─ images
//         ├──> imgX.png
//         └──> imgY.png