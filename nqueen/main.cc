/*
 * =====================================================================================
 *
 *       Filename:  main.cc
 *
 *    Description:  ls
 *
 *        Version:  1.0
 *        Created:  04/16/2014 07:26:42 AM
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  BOSS14420 (), firefoxlinux at gmail dot com
 *   Organization:  
 *
 * =====================================================================================
 */


#include <iostream>
#include <vector>
#include <deque>
#include <unordered_set>
#include <algorithm>
#include <random>
#include <ctime>

using std::size_t;

size_t table_size;

std::vector<size_t> vcount1; // f[i]
std::vector<size_t> vcount2; // f[i] + i
std::vector<size_t> vcount3; // f[i] - i

std::vector<std::unordered_set<size_t> > vvalue1;
std::vector<std::unordered_set<size_t> > vvalue2;
std::vector<std::unordered_set<size_t> > vvalue3;

std::vector<size_t> violation;
size_t total_violation;
size_t max_violation;
std::deque<size_t> max_vio_cols;

size_t try_move(size_t col, size_t to, std::vector<size_t> &pos) 
{
    size_t from = pos[col];
    size_t vc1f = vcount1[from] - 1, 
           vc1t = vcount1[to] + 1,
           vc2f = vcount2[from + col] - 1, 
           vc2t = vcount2[to + col] + 1,
           vc3f = vcount3[from - col + table_size - 1] - 1, 
           vc3t = vcount3[to - col + table_size - 1] + 1;

    size_t new_violation = total_violation - 6
                            - 2*vc1f + 2*vc1t
                            - 2*vc2f + 2*vc2t
                            - 2*vc3f + 2*vc3t;

    return new_violation;
}

void calculate_violation(std::vector<size_t> const &pos)
{
    vcount1.assign(table_size, 0);
    vcount2.assign(table_size * 2 - 1, 0);
    vcount3.assign(table_size * 2 - 1, 0);
    for (size_t col = 0; col != table_size; ++col) {
        ++vcount1[pos[col]];
        ++vcount2[pos[col] + col];
        ++vcount3[pos[col] - col + table_size - 1];
    }

    violation.assign(table_size, 0);
    total_violation = 0;
    max_vio_cols.clear();
    max_violation = 0;
    for (size_t col = 0; col != table_size; ++col) {
        size_t vc;
        violation[col] += ((vc = vcount1[pos[col]]) <= 1) ? 0 : vc - 1;
        violation[col] += ((vc = vcount2[pos[col] + col]) <= 1) ? 0 : vc - 1;
        violation[col] += ((vc = vcount3[pos[col] + table_size - col - 1]) <= 1) ? 0 : vc - 1;
        total_violation += violation[col];

        if (violation[col] > max_violation) {
            max_vio_cols.clear();
            max_vio_cols.push_back(col);
            max_violation = violation[col];
        } else if (violation[col] == max_violation) {
            max_vio_cols.push_back(col);
        }
    }
}

void move(size_t col, size_t to, std::vector<size_t> &pos)
{
    size_t from = pos[col];
    pos[col] = to;
    size_t vc1f = --vcount1[from], 
           vc1t = ++vcount1[to],
           vc2f = --vcount2[from + col], 
           vc2t = ++vcount2[to + col],
           vc3f = --vcount3[from + table_size - col - 1], 
           vc3t = ++vcount3[to + table_size - col - 1];

//    for (auto col2 : vvalue1[from]) vvalue1[col2].remove(from);
//    for (auto col2 : vvalue2[from + col]) vvalue2[from + col2

    std::fill(violation.begin(), violation.end(), 0);
    total_violation = 0;
    max_vio_cols.clear();
    max_violation = 0;
    for (size_t col = 0; col != table_size; ++col) {
        size_t vc;
        violation[col] += ((vc = vcount1[pos[col]]) <= 1) ? 0 : vc - 1;
        violation[col] += ((vc = vcount2[pos[col] + col]) <= 1) ? 0 : vc - 1;
        violation[col] += ((vc = vcount3[pos[col] - col + table_size - 1]) <= 1) ? 0 : vc - 1;
        total_violation += violation[col];

        if (violation[col] > max_violation) {
            max_vio_cols.clear();
            max_vio_cols.push_back(col);
            max_violation = violation[col];
        } else if (violation[col] == max_violation) {
            max_vio_cols.push_back(col);
        }
    }
}

void search() {
    // initialize
    std::vector<size_t> pos(table_size);
    std::iota(pos.begin(), pos.end(), 0);
    std::random_device rd;
    std::mt19937 g(rd());
    std::shuffle(pos.begin(), pos.end(), g);

    calculate_violation(pos);

    std::cout << "Iteration 0: violation = " << total_violation << '\n';

    // local search
    size_t iter = 1;
    std::srand(std::time(0));
//    std::srand(0);
    while (total_violation) {
        size_t max_violation_col = max_vio_cols[std::rand() % max_vio_cols.size()];
        size_t best_violation = -1;
        std::deque<size_t> best_move;
        size_t old_pos = pos[max_violation_col];
        for (size_t new_row = 0; new_row != table_size; ++new_row)
            if (new_row != old_pos) {
                size_t vio = try_move(max_violation_col, new_row, pos);
                if (vio < best_violation) {
                    best_violation = vio;
                    best_move.clear();
                    best_move.push_back(new_row);
                } else if (vio == best_violation) {
                    best_move.push_back(new_row);
                }
            }
        
//        pos[max_violation_col] = best_move[std::rand() % best_move.size()];
//        calculate_violation(pos);
        move(max_violation_col, best_move[std::rand() % best_move.size()], pos);
        ++iter;
//        std::cout << "Iteration " << iter++ 
//            << " : violation = " << total_violation << '\n';
//        std::cout << "Result: ";
//        for (auto row : pos) std::cout << row << ' ';
//        std::cout << '\n';
    }
    
    std::cout << iter << " iterations\n";
//    std::cout << "\nResult:\n";
//    for (auto row : pos) std::cout << row << ' ';
}

int main(int argc, char *argv[]) 
{
    table_size = std::atoi(argv[1]);
    search();
}
