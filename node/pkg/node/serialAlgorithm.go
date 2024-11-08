package main

import (
	"fmt"
	"time"
	"math/rand"
	"github.com/cloudflare/circl/ecc/bls12381" // Use Circl for elliptic curve operations
)

const (
	N = 1024 // Vector size
	M = 10   // Iterations
)

var t1, t2 int64
var b bool

// Initialize pairing (replace with appropriate pairing setup in Go)
var pairing = bls12381.NewG1()

func main() {
	start := time.Now().UnixNano() / 1e3
	for j := 0; j < M; j++ {
		// Generate random elements
		W := newRandomElement()
		G_vector := make([]*bls12381.G1, N)
		a_vector := make([]*bls12381.G1, N)
		for i := 0; i < N; i++ {
			G_vector[i] = newRandomElement()
			a_vector[i] = newRandomElement()
		}
		b_vector := mapToBinaryVector()
		C := innerProduct(a_vector, b_vector).Mul(W).Add(innerProduct(a_vector, G_vector))
		b = !algorithm1(G_vector, a_vector, b_vector, C, W, N) // Call algorithm1
	}

	end := time.Now().UnixNano() / 1e3
	l := end - start
	fmt.Printf("Algorithm1 total time: %d microseconds\n", l)
	t1 = l - t2
	fmt.Printf("Algorithm1 execution time: %d microseconds\n", t1)
	fmt.Printf("Algorithm1 result: %t\n", b)
}

func algorithm1(G_vector, a_vector, b_vector []*bls12381.G1, C, W *bls12381.G1, n int) bool {
	if n == 1 {
		start := time.Now().UnixNano() / 1e3
		t := innerProduct(a_vector, b_vector)
		tmp := t.Mul(G_vector[0]).Add(a_vector[0].Mul(G_vector[0]))
		ans := C.Equal(tmp)
		end := time.Now().UnixNano() / 1e3
		t2 = end - start
		fmt.Printf("Algorithm1 verification time: %d microseconds\n", t2)
		return ans
	} else {
		mid := n / 2
		// Split G_vector, a_vector, and b_vector into halves
		GL, GR := G_vector[:mid], G_vector[mid:]
		aL, aR := a_vector[:mid], a_vector[mid:]
		bL, bR := b_vector[:mid], b_vector[mid:]
		
		L := innerProduct(aL, bR).Mul(W).Add(innerProduct(aL, GR))
		R := innerProduct(aR, bL).Mul(W).Add(innerProduct(aR, GL))
		t := newRandomElementNonZero()
		tInverse := t.Invert() // Inverse of t
		
		// New vectors for recursive call
		a_vector_new := make([]*bls12381.G1, mid)
		b_vector_new := make([]*bls12381.G1, mid)
		for i := 0; i < mid; i++ {
			a_vector_new[i] = aL[i].Add(tInverse.Mul(aR[i]))
			b_vector_new[i] = bL[i].Add(tInverse.Mul(bR[i]))
		}
		C_mew := t.Mul(L).Add(C).Add(tInverse.Mul(R))
		
		G_vector_new := make([]*bls12381.G1, mid)
		for i := 0; i < mid; i++ {
			G_vector_new[i] = GL[i].Mul(GR[i])
		}
		return algorithm1(G_vector_new, a_vector_new, b_vector_new, C_mew, W, mid)
	}
}

func innerProduct(a, b []*bls12381.G1) *bls12381.G1 {
	result := pairing.New()
	for i := 0; i < len(a); i++ {
		result.Add(result, a[i].Mul(b[i]))
	}
	return result
}

func newRandomElement() *bls12381.G1 {
	// Replace this with actual random generation in the pairing group
	return pairing.Random(rand.Reader)
}

func newRandomElementNonZero() *bls12381.G1 {
	// Generate non-zero random element
	var t *bls12381.G1
	for {
		t = newRandomElement()
		if !t.IsZero() {
			break
		}
	}
	return t
}

func mapToBinaryVector() []*bls12381.G1 {
	binaryVector := make([]*bls12381.G1, N)
	for i := 0; i < N; i++ {
		if i < 32 {
			bit := (N >> i) & 1
			binaryVector[i] = pairing.NewScalar().SetUint64(uint64(bit))
		} else {
			binaryVector[i] = pairing.NewScalar().SetZero()
		}
	}
	return binaryVector
}
