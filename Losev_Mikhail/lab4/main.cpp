#include <iostream>
#include <string>
#include <vector>
#include <set>
#include <thread>	// parallel 
#include <mutex>
#include <functional>		// std::ref
#include <cstdio>
#include <cctype>

using namespace std;

vector<int> prefix_function(const string& s) {
	int len = s.length();
	vector<int> p(len); 
				
	p[0] = 0;

	int k = 0;	
	for (int i = 1; i < len; ++i) {	
		while ((k > 0) && (s[k] != s[i])) 
			k = p[k - 1]; 
		if (s[k] == s[i])
			++k;
		p[i] = k;
	}
	return p;
}

void print(const vector<int> &v) {
	if (v.size() == 0) {
		cout << "-1" << endl;
		return;
	}
	for (int i = 0; i < v.size() - 1; i++)
		cout << v[i] << ",";
	cout << v[v.size() - 1] << endl;
}

void print_answ(const set<int> &s) {
	if (s.size() == 0) {
		cout << "-1" << endl;
		return;
	}
	auto end = s.end();
	end--;
	auto it = s.begin();
	for (auto it = s.begin(); it != end; it++)
		cout << *it << ",";
	cout << *end << endl;
}



set<int> KMP(string& T, string& P) {
	
	set<int> solution;
	if (P.length() >= T.length()) {
		solution.insert(-1);
		return solution;
	}
	vector<int> prefixes = prefix_function(P);
	
	int j = 0;
	for (int i = 0; i < T.length(); i++) {
		while (j == P.length() || (j > 0 && P[j] != T[i])) {
			j = prefixes[j - 1];
			if (P.length() - j > T.length() - i)
				break;
		}

		if (T[i] == P[j])
			j++;

		if (j == P.length()) 
			solution.insert(i - j + 1);
	}

	return solution;
}

void one_threaded_KMP(mutex &cout_mutex, mutex &solution_mutex, set<int>& solution, string& T, string& P, vector<int>& prefixes, int start, int end) 
{
	cout_mutex.lock();
	cout << "A thread " << this_thread::get_id() << " started ";
	cout << "with start = " << start << "; end = " << end << ";" << endl;
	cout_mutex.unlock();

	set<int> partly;
	
	int j = 0;
	for (int i = start; i < end + P.length() - 1; i++) {
		while (j == P.length() || (j > 0 && P[j] != T[i])) {
			j = prefixes[j - 1];
			if (P.length() - j > end + P.length() - 1 - i)
				break;
		}

		if (T[i] == P[j])
			j++;
		
		if (j == P.length()) {
				solution_mutex.lock();
				solution.insert(i - j + 1);
				solution_mutex.unlock();
				partly.insert(i - j + 1);
		}
	}
	
	cout_mutex.lock();
	cout << "The threads " << this_thread::get_id() << " result: ";
	print_answ(partly);
	cout_mutex.unlock();

	cout_mutex.lock();
	cout << "A thread " << this_thread::get_id() << " is now ready to be joined" << endl;
	cout_mutex.unlock();
}

set<int> multithreaded_KMP(string& T, string& P, int k) {
	set<int> solution;
	if (P.length() >= T.length()) {
		solution.insert(-1);
		return solution;
	}

	vector<int> prefixes = prefix_function(P);	// prefix-function
	

	auto part_len = (T.length() / k); 
	mutex solution_mutex;
	mutex cout_mutex;
	thread thr[k];

	int start = 0;
	int end = part_len;
	for (int i = 0; i < k - 1; i++) {	// create treads
		thr[i] = thread(one_threaded_KMP, ref(cout_mutex), ref(solution_mutex), ref(solution), ref(T), ref(P), ref(prefixes), start, end);
		start = end;
		end += part_len;
	}
	thr[k - 1] = thread(one_threaded_KMP, ref(cout_mutex), ref(solution_mutex), ref(solution), ref(T), ref(P), ref(prefixes), start, T.length());

	for (int i = 0; i < k; i++) {		// join threads
		auto current_thread_id = thr[i].get_id();
		thr[i].join();	
		cout_mutex.lock();
		cout << "The thread " << current_thread_id << " is now  joined" << endl;
		cout_mutex.unlock();
	}

	return solution;
}

int cyclic_shift(string& A, string& B) {
	if (A == B) return 0;
	if (A.length() != B.length()) return -1;
 	//vector<int> p = prefix_function(A + "$" + B);
	int A_prefix = prefix_function(A + "$" + B)[A.length() + B.length()]; 			 // prefix function of the last char
	//p = prefix_function(B + "$" + A);
	int A_postfix = prefix_function(B + "$" + A)[A.length() + B.length()];

	if (A_postfix + A_prefix != A.size() || A.size() != B.size())
		return -1;

	return A_prefix;
}

void test_KMP(string& p, string& t, int k) {
	auto answ = multithreaded_KMP(t, p, k);

	cout << "1-thread KMP: ";
	print_answ(KMP(t, p));
	cout << k << "-thread KMP: ";
	print_answ(answ);
}

void test_cyclic_shift(string& a, string& b) {
	int index;
	index = cyclic_shift(a, b);
	cout << index << endl;
	if (index != -1) {
		cout << a << " = " << a.substr(0, index) << " + " << a.substr(index, a.length()) << endl;
		cout << b << " = " << a.substr(index, a.length()) << " + " << a.substr(0, index) << endl;
	}
}

int main()
{
	string a, b;
	cin >> a >> b;

	//test_KMP(a, b, 3);	// the last argument is number of threads
	test_cyclic_shift(a, b);


	return 0;
}
