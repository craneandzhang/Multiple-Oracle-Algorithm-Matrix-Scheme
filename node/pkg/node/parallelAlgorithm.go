package main

import (
	"fmt"
	"math/rand"
	"time"
	"github.com/cloudflare/circl/ecc/bls12381" // Use Circl for elliptic curve operations
)

const (
	N = 1024 // Vector size
	M = 10   // Iteration size
)

var (
	t1 int64
	t2 int64
	pairing = bls12381.NewG1()
)

func main() {
	// Initialize elements O, W, and G
	O := pairing.Identity()
	W := pairing.Identity()
	G := pairing.Identity()

	// Create binary vector
	b_vector := mapToBinaryVector()

	// Create a MxN matrix of random elements
	a_matrix := make([][]*bls12381.G1, M)
	for i := 0; i < M; i++ {
		a_matrix[i] = make([]*bls12381.G1, N)
		for j := 0; j < N; j++ {
			a_matrix[i][j] = newRandomElement()
		}
	}

	// Compute C_group for each row in a_matrix
	C_group := make([]*bls12381.G1, M)
	for i := 0; i < M; i++ {
		C_group[i] = innerProduct(a_matrix[i], b_vector).Mul(W)
	}

	// Measure time for algorithm3
	start := time.Now().UnixNano() / 1e3
	b := algorithm3(C_group, a_matrix, b_vector, W, N)
	end := time.Now().UnixNano() / 1e3

	l := end - start
	fmt.Printf("Algorithm3 total time: %d microseconds\n", l)
	t1 = l - t2
	fmt.Printf("Algorithm3 execution time: %d microseconds\n", t1)
	fmt.Println("Result:", b)
}

// Parallel computation algorithm
func algorithm3(C_group []*bls12381.G1, a_matrix [][]*bls12381.G1, b_vector []*bls12381.G1, W *bls12381.G1, n int) bool {
	R_group := make([]*bls12381.G1, M)
	r_group := make([]*bls12381.G1, M)
	u_group := make([]*bls12381.G1, M)
	z_group := make([]*bls12381.G1, M)
	x_group := make([]*bls12381.G1, M)

	b := true
	start := time.Now().UnixNano() / 1e3
	for i := 0; i < M; i++ {
		r_group[i] = newRandomElement()
		R_group[i] = r_group[i].Mul(W)
		u_group[i] = newRandomElement()
		x_group[i] = newRandomElement()
		z_group[i] = r_group[i].Add(u_group[i].Mul(innerProduct(a_matrix[i], b_vector)))

		left := z_group[i].Mul(W)
		right := R_group[i].Add(u_group[i].Mul(C_group[i]))

		if !left.IsEqual(right) {
			b = false
			break
		}
	}
	end := time.Now().UnixNano() / 1e3
	t2 = end - start
	fmt.Printf("Algorithm3 calculation time: %d microseconds\n", t2)
	return b
}

// Calculate inner product of two vectors
func innerProduct(a, b []*bls12381.G1) *bls12381.G1 {
	result := pairing.Identity()
	for i := 0; i < len(a); i++ {
		result = result.Add(a[i].Mul(b[i]))
	}
	return result
}

// Generate a random non-zero element from Zp
func newRandomElement() *bls12381.G1 {
	for {
		elem := pairing.Random(rand.Reader)
		if !elem.IsIdentity() {
			return elem
		}
	}
}

// Map integer N to binary vector
func mapToBinaryVector() []*bls12381.G1 {
	binaryVector := make([]*bls12381.G1, N)
	for i := 0; i < N; i++ {
		bit := (N >> i) & 1
		if bit == 1 {
			binaryVector[i] = pairing.Identity().Neg() // Set to 1 equivalent
		} else {
			binaryVector[i] = pairing.Identity() // Set to 0 equivalent
		}
	}
	return binaryVector
}
