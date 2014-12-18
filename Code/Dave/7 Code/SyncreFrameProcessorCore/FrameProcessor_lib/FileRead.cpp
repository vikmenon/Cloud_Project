#include <iostream>
#include "dirent.h"

using namespace std;
using std::cout;
using std::endl;

void ListAllFilesInDir(const char *directory) 
{
		/*WIN32_FIND_DATA file_data;
		HANDLE hFind = INVALID_HANDLE_VALUE;
		if ((hFind = FindFirstFile((LPCWSTR) directory, &file_data)) == INVALID_HANDLE_VALUE) {
			cout << "error\n";
			return;
		}
		cout << file_data.cFileName << endl;

		while (FindNextFile(hFind, &file_data)) {
			cout << file_data.cFileName << endl;
		}
		FindClose(hFind);*/
}

void GetFiles(std::string filesPath, const int numberFiles, string* filenames_array)
{
	DIR *dir;
	struct dirent *ent;
	
	//create files array
	if ((dir = opendir(filesPath.c_str())) != NULL)
	{
		/* get all the files and directories within directory */
		long i = 0;
		while ((ent = readdir(dir)) != NULL) 
		{
			filenames_array[i] = ent->d_name;

			i++;
		}
		closedir(dir);
	}
	else 
	{
		/* could not open directory */
		perror("");
		//return NULL;
	}
}

