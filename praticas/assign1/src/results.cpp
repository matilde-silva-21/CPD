#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>
#include "string.h"
#include <fstream>

using namespace std;

#define SYSTEMTIME clock_t
 
string OnMult(int m_ar, int m_br) 
{
	
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);



    Time1 = clock();

	for(i=0; i<m_ar; i++)
	{	for( j=0; j<m_br; j++)
		{	temp = 0;
			for( k=0; k<m_ar; k++)
			{	
				temp += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}


    Time2 = clock();
    string execution_time = to_string((double)(Time2 - Time1) / CLOCKS_PER_SEC);
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}

	cout << endl;
	std::cout << "\n";

    free(pha);
    free(phb);
    free(phc);

    return execution_time;
}

// add code here for line x line matriz multiplication
string OnMultLine(int m_ar, int m_br)
{
    
    SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);



    Time1 = clock();


	for(i=0; i<m_ar; i++) // linha atual da matriz A
	{	for(k=0; k<m_ar; k++) // coluna A ; linha B
		{	
			for(j=0; j<m_br; j++) // coluna atual da matriz B
			{
				// MT_A(i, k) * MT_B(k, j)
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];

			}
			
		}
	}


    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}

	cout << endl;
	std::cout << "\n";

    free(pha);
    free(phb);
    free(phc);

    string execution_time = to_string((double)(Time2 - Time1) / CLOCKS_PER_SEC);
    return execution_time;
}

// add code here for block x block matriz multiplication
string OnMultBlock(int m_ar, int m_br, int bkSize)
{
    
    SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);



    Time1 = clock();

    for (int jj = 0; jj < m_ar; jj += bkSize) {
        for (int kk = 0; kk < m_ar; kk += bkSize) {
            for (i = 0; i < m_ar; i++) {
                for (j = jj; j < ((jj + bkSize) > m_ar ? m_ar : (jj + bkSize)); j++) {
                    for (k = kk; k < ((kk + bkSize) > m_ar ? m_ar : (kk + bkSize)); k++) {
                        phc[i * m_ar + k] += pha[i * m_ar + j] * phb[j * m_br + k];
                    }
                }
            }
        }
    }


    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}

	cout << endl;
	std::cout << "\n";

    free(pha);
    free(phb);
    free(phc);

    string execution_time = to_string((double)(Time2 - Time1) / CLOCKS_PER_SEC);


    return execution_time;

}



void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}


int main (int argc, char *argv[])
{

	char c;
	int op, choice = 0;

    int startSize = 0, endSize = 0, increment = 0, numberOfTests = 0, startBlockSize = 0, endBlockSize = 0, blockIncrement = 0;

    cout << endl << "------TEST MODE------\n" << endl;
    cout << "1. Multiplication" << endl;
    cout << "2. Line Multiplication" << endl;
    cout << "3. Block Multiplication" << endl;
    cout << "Selection?: ";
    cin >> choice;

    cout << ("Start matrix size? ");
    cin >> startSize;
    
    cout << ("End matrix size? ");
    cin >> endSize;

    cout << ("Matrix size increment? ");
    cin >> increment;

    if(choice == 3){
        cout << ("Start block size? ");
        cin >> startBlockSize;
        
        cout << ("End block size? ");
        cin >> endBlockSize;

        cout << ("Block size increment? ");
        cin >> blockIncrement;
    }

    cout << "Number of Tests? ";
    cin >> numberOfTests;

    std::cout << "\n";
    

    fstream file;
	
	int EventSet = PAPI_NULL;
  	long long values[4];
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCA);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCA" << endl;


	ret = PAPI_add_event(EventSet,PAPI_DP_OPS);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_DP_OPS" << endl;
    

    switch (choice)
    {
    case 1:
        for(int size = startSize; size <= endSize; size +=increment){
            string filename;
            filename = "../doc/test_results/OnMult_test.csv";
            

            file.open(filename, ios::out | ios::app);

            
            if(!file){
                cout << "File creation failed\n";
                return -1;
            }

            file << "\nMatrix Size,Execution Time,L1 DCM,L2 DCM,L2 DCA,DP_OPS\n";

            for(int i=0; i<numberOfTests; i++){

                // Start counting
                ret = PAPI_start(EventSet);
                if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

                string executionTime = OnMult(size, size);

                ret = PAPI_stop(EventSet, values);
                if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;

                printf("L1 DCM: %lld \n",values[0]);
                printf("L2 DCM: %lld \n",values[1]);
                printf("L2 DCA: %lld \n",values[2]);
                printf("DP_OPS: %lld \n",values[3]);

                file << size << "," << executionTime << "," << values[0] << "," << values[1] << "," << values[2] << "," << values[3] << "\n";

                ret = PAPI_reset( EventSet );
                if ( ret != PAPI_OK )
                    std::cout << "FAIL reset" << endl; 
                std::cout << "\n";
            }

            file.close();

        }
            
        break;
    

    case 2:
        for(int size = startSize; size <= endSize; size +=increment){
            string filename;
            filename = "../doc/test_results/OnMultLine_test.csv";
            

            file.open(filename, ios::out | ios::app);

            
            if(!file){
                cout << "File creation failed\n";
                return -1;
            }

            file << "\nMatrix Size,Execution Time,L1 DCM,L2 DCM,L2 DCA,DP_OPS\n";

            for(int i=0; i<numberOfTests; i++){

                // Start counting
                ret = PAPI_start(EventSet);
                if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

                string executionTime = OnMultLine(size, size);

                ret = PAPI_stop(EventSet, values);
                if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
                printf("L1 DCM: %lld \n",values[0]);
                printf("L2 DCM: %lld \n",values[1]);
                printf("L2 DCA: %lld \n",values[2]);
                printf("DP_OPS: %lld \n",values[3]);

                file << size << "," << executionTime << "," << values[0] << "," << values[1] << "," << values[2] << "," << values[3] << "\n";

                ret = PAPI_reset( EventSet );
                if ( ret != PAPI_OK )
                    std::cout << "FAIL reset" << endl; 
                std::cout << "\n";
            }

            file.close();

        }
    
        break;
        
    case 3: 
        for(int size = startSize; size <= endSize; size +=increment){
            string filename;
            filename = "../doc/test_results/OnMultBlock_test.csv";
            

            file.open(filename, ios::out | ios::app);

            
            if(!file){
                cout << "File creation failed\n";
                return -1;
            }

            for(int blockSize=startBlockSize; blockSize <= endBlockSize; blockSize+=blockIncrement){

                file << "\nMatrix Size,Block Size,Execution Time,L1 DCM,L2 DCM,L2 DCA,DP_OPS\n";

                for(int i=0; i<numberOfTests; i++){

                    // Start counting
                    ret = PAPI_start(EventSet);
                    if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;
                    
                    string executionTime = OnMultBlock(size, size, blockSize);


                    ret = PAPI_stop(EventSet, values);
                    if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;

                    printf("L1 DCM: %lld \n",values[0]);
                    printf("L2 DCM: %lld \n",values[1]);
                    printf("L2 DCA: %lld \n",values[2]);
                    printf("DP_OPS: %lld \n",values[3]);

                    file << size << "," << blockSize << "," << executionTime << "," << values[0] << "," << values[1] << "," << values[2] << "," << values[3] << "\n";

                    ret = PAPI_reset( EventSet );
                    if ( ret != PAPI_OK )
                        std::cout << "FAIL reset" << endl; 
                    std::cout << "\n";
                }
            }


            file.close();

        }
    
        break;
            
    default:
        break;
    }

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCA );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_DP_OPS);
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;
    
}

